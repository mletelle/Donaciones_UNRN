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
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;


public class ListadoOrdenesAsignadasVoluntario extends JFrame {

    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private VoluntarioDTO voluntarioActual;

    public ListadoOrdenesAsignadasVoluntario(IApi api, VoluntarioDTO voluntarioActual) {
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
                    
                    // --- USO DE CAMPOVACIOEXCEPTION (Validar selección de fila) ---
                    if (filaSeleccionada == -1) {
                        throw new CampoVacioException("Seleccione una orden de retiro de la lista para gestionar.");
                    }
                    // ---------------------------------------------------------------
                    
                    int idOrden = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    
                    // Abrir la ventana de gestión, pasando la referencia de esta ventana
                    GestionarOrdenVoluntario ventanaGestionar = new GestionarOrdenVoluntario(api, idOrden, ListadoOrdenesAsignadasVoluntario.this);
                    
                    // listener para refrescar cuando se cierre la ventana de gestión
                    ventanaGestionar.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            // refrescar la tabla cuando se cierre la ventana de gestion
                            ListadoOrdenesAsignadasVoluntario.this.refrescarTabla();
                        }
                    });
                    
                    ventanaGestionar.setVisible(true);
                    
                } catch (CampoVacioException ex) {
                    // Manejo de la excepción custom para falta de selección
                    JOptionPane.showMessageDialog(ListadoOrdenesAsignadasVoluntario.this, ex.getMessage(), "Error de Selección", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    // Manejo de cualquier otro error inesperado
                    JOptionPane.showMessageDialog(ListadoOrdenesAsignadasVoluntario.this, "Error al gestionar la orden: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        panelPrincipal.add(btnGestionar, BorderLayout.SOUTH);

        add(panelPrincipal);
        cargarOrdenesAsignadas();
    }

    // Metodos
    // metodo para cargar ordenes
    private void cargarOrdenesAsignadas() {
        modeloTabla.setRowCount(0); // Limpiar la tabla
        
        try {
            // Asumimos que voluntarioActual no es nulo, o la llamada fallaría antes
            List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesAsignadas(voluntarioActual.getUsuario());
            
            // --- USO DE OBJETONULOEXCEPTION (Si la API retorna null) ---
            if (ordenes == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de órdenes.");
            }
            // -----------------------------------------------------------
            
            for (OrdenRetiroDTO orden : ordenes) {
                // Se asume que getFechaCreacion() y getEstado() son Strings o tienen toString() adecuado
                modeloTabla.addRow(new Object[]{orden.getId(), orden.getFechaCreacion(), orden.getEstado(), orden.getDescripcion()});
            }
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom para datos nulos
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga de Datos", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
             // Manejo de otros errores de la API
            JOptionPane.showMessageDialog(this, "Error al obtener órdenes asignadas: " + ex.getMessage(), "Error de API", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // metodo para refrescar la tabla desde otras ventanas
    public void refrescarTabla() {
        cargarOrdenesAsignadas();
        tablaOrdenes.repaint();
        tablaOrdenes.revalidate();
    }
    
}