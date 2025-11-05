package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
 
public class ListadoOrdenesRetiro extends JFrame {

    private JTable tablaOrdenes;
    private IApi api;

    public ListadoOrdenesRetiro(IApi api) {
        this.api = api;
        setTitle("Listado de Órdenes de Retiro");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        cargarDatos();
    }
 
    private void initUI() {
        tablaOrdenes = new JTable();
        tablaOrdenes.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] {"Estado", "Fecha Creación", "Vehículo", "Voluntario"} 
        ));

        tablaOrdenes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = tablaOrdenes.getSelectedRow();
                    if (selectedRow != -1) {
                        int idOrden = (int) tablaOrdenes.getValueAt(selectedRow, 0);
                        new GestionarOrdenRetiro(api, idOrden).setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tablaOrdenes.getModel();
        model.setRowCount(0);

        List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesDeRetiro("PENDIENTE");
        for (OrdenRetiroDTO orden : ordenes) {
            String estadoTexto = orden.getEstado() == 1 ? "Pendiente" : orden.getEstado() == 2 ? "En Ejecución" : "Completado";
            String vehiculo = orden.getVehiculo() != null ? orden.getVehiculo() : "Vehículo Desconocido";
            String voluntario = orden.getVoluntario() != null ? orden.getVoluntario() : "Voluntario Desconocido";

            model.addRow(new Object[] {estadoTexto, orden.getFechaCreacion(), vehiculo, voluntario}); 
        }
    }
}