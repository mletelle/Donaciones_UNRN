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

public class ListadoOrdenesAsignadasVoluntario extends JFrame {

    private JTable tablaOrdenes;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private VoluntarioDTO voluntarioActual;

    public ListadoOrdenesAsignadasVoluntario(IApi api, VoluntarioDTO voluntarioActual) {
        this.api = api;
        this.voluntarioActual = voluntarioActual;

        setTitle("ordenes Asignadas");
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
                int filaSeleccionada = tablaOrdenes.getSelectedRow();
                if (filaSeleccionada != -1) {
                    int idOrden = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    // referencia para que GestionarOrdenVoluntario pueda notificar cuando haya cambios
                    GestionarOrdenVoluntario ventanaGestionar = new GestionarOrdenVoluntario(api, idOrden, ListadoOrdenesAsignadasVoluntario.this);
                    
                    //  listener para refrescar cuando se cierre la ventana
                    ventanaGestionar.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            // refrescar la tabla cuando se cierre la ventana de gestion
                            ListadoOrdenesAsignadasVoluntario.this.refrescarTabla();
                        }
                    });
                    
                    ventanaGestionar.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(ListadoOrdenesAsignadasVoluntario.this, "Seleccione una orden para gestionar.", "Error", JOptionPane.ERROR_MESSAGE);
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
        List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesAsignadas(voluntarioActual.getUsuario());
        for (OrdenRetiroDTO orden : ordenes) {
            modeloTabla.addRow(new Object[]{orden.getId(), orden.getFechaCreacion(), orden.getEstado(), orden.getDescripcion()});
        }
    }
    
    //  metodo para refrescar la tabla desde otras ventanas
    public void refrescarTabla() {
        cargarOrdenesAsignadas();
        tablaOrdenes.repaint();
        tablaOrdenes.revalidate();
    }
    
}