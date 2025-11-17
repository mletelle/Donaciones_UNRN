package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.ObjetoNuloException;


public class ListadoOrdenesRetiro extends JFrame {

    private JTable tablaOrdenes;
    private IApi api;

    public ListadoOrdenesRetiro(IApi api) {
        this.api = api;
        setTitle("Listado de Ordenes de Retiro");
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
    private void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tablaOrdenes.getModel();
        model.setRowCount(0);

        try {
            List<OrdenRetiroDTO> ordenes = api.obtenerTodasOrdenesRetiro();
            
            if (ordenes == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de órdenes.");
            }

            for (OrdenRetiroDTO orden : ordenes) {
                // Se mantienen los chequeos de nulidad para asegurar la visualización
                String estadoTexto = orden.getEstado() != null ? orden.getEstado() : "N/A"; 
                String vehiculo = orden.getVehiculo() != null ? orden.getVehiculo() : "Vehiculo Desconocido";
                String voluntario = orden.getVoluntario() != null ? orden.getVoluntario() : "Voluntario Desconocido";

                model.addRow(new Object[] {estadoTexto, orden.getFechaCreacion(), vehiculo, voluntario}); 
            }
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom para datos nulos
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }
}