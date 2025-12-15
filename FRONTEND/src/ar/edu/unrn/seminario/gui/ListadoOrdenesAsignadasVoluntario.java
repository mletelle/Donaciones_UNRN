package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;


public class ListadoOrdenesAsignadasVoluntario extends JFrame {

    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private UsuarioDTO voluntarioActual;

    public ListadoOrdenesAsignadasVoluntario(IApi api, UsuarioDTO voluntarioActual) {
        this.api = api;
        this.voluntarioActual = voluntarioActual;

        setTitle("Órdenes Asignadas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Fecha", "Estado", "Descripcion"}, 0);
        tablaOrdenes = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER); 

        JButton btnGestionar = new JButton("Gestionar Orden Retiro");
        
        // Accion del boton "Gestionar Orden Retiro"
        btnGestionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int filaSeleccionada = tablaOrdenes.getSelectedRow();

                    if (filaSeleccionada == -1) {
                        throw new CampoVacioException("Seleccione una orden de retiro de la lista para gestionar.");
                    }

                    String estado = (String) modeloTabla.getValueAt(filaSeleccionada, 2);

                    if ("Completado".equalsIgnoreCase(estado) || "Cancelado".equalsIgnoreCase(estado)) {
                        throw new ReglaNegocioException("No se puede gestionar una orden que ya esta '" + estado + "'.");
                    }

                    int idOrden = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

                    // valida si hay pedidos antes de abrir la ventana hija
                    List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
                    if (pedidos == null || pedidos.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            ListadoOrdenesAsignadasVoluntario.this,
                            "No hay pedidos asignados a esta orden para este voluntario.",
                            "Sin pedidos",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    GestionarOrdenVoluntario ventanaGestionar =
                        new GestionarOrdenVoluntario(api, idOrden, ListadoOrdenesAsignadasVoluntario.this);

                    ventanaGestionar.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            ListadoOrdenesAsignadasVoluntario.this.refrescarTabla();
                        }
                    });

                    ventanaGestionar.setVisible(true);

                } catch (CampoVacioException ex) {
                    JOptionPane.showMessageDialog(
                        ListadoOrdenesAsignadasVoluntario.this,
                        ex.getMessage(),
                        "Error de Selección",
                        JOptionPane.WARNING_MESSAGE
                    );
                } catch (ReglaNegocioException ex) {
                    JOptionPane.showMessageDialog(
                        ListadoOrdenesAsignadasVoluntario.this,
                        ex.getMessage(),
                        "Error de Validación",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        
        panelPrincipal.add(btnGestionar, BorderLayout.SOUTH);

        add(panelPrincipal);
        cargarOrdenesAsignadas();
    }

    // Metodos
    private void cargarOrdenesAsignadas() {
        modeloTabla.setRowCount(0); // Limpiar la tabla
        
        try {
            // Asumimos que voluntarioActual no es nulo, o la llamada fallaría antes
            List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesAsignadas(voluntarioActual.getUsuario());
            
            if (ordenes == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de órdenes.");
            }
            
            for (OrdenRetiroDTO orden : ordenes) {
                // Se asume que getFechaCreacion() y getEstado() son Strings o tienen toString() adecuado
                modeloTabla.addRow(new Object[]{orden.getId(), orden.getFechaCreacion(), orden.getEstado(), orden.getDescripcion()});
            }
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom para datos nulos
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // metodo para refrescar la tabla desde otras ventanas
    public void refrescarTabla() {
        cargarOrdenesAsignadas();
        tablaOrdenes.repaint();
        tablaOrdenes.revalidate();
    }
    
}