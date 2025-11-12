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
        setTitle("Listado de Ordenes de Retiro Pendientes");
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
            new String[] {"Estado", "Fecha Creacion", "Vehiculo", "Voluntario"} 
        ));

        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Metodos
    // metodo para cargar datos
    private void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tablaOrdenes.getModel();
        model.setRowCount(0);

        List<OrdenRetiroDTO> ordenes = api.obtenerOrdenesDeRetiro("PENDIENTE");
        for (OrdenRetiroDTO orden : ordenes) {
            String estadoTexto = orden.getEstado(); // ahora getEstado() devuelve String directamente
            String vehiculo = orden.getVehiculo() != null ? orden.getVehiculo() : "Vehiculo Desconocido";
            String voluntario = orden.getVoluntario() != null ? orden.getVoluntario() : "Voluntario Desconocido";

            model.addRow(new Object[] {estadoTexto, orden.getFechaCreacion(), vehiculo, voluntario}); 
        }
    }
    
}