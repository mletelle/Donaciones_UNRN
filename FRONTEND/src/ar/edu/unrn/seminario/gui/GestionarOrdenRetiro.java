package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenRetiroDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class GestionarOrdenRetiro extends JFrame {

    private JTable tablaVisitas;
    private IApi api;
    private int idOrden;

    public GestionarOrdenRetiro(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;
        setTitle("Gestionar Orden de Retiro");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        cargarDatos();
    }
 
    private void initUI() {
        setLayout(new BorderLayout());

        tablaVisitas = new JTable();
        tablaVisitas.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] {"Fecha", "Observación", "Bienes"}
        ));

        JScrollPane scrollPane = new JScrollPane(tablaVisitas);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnRegistrarVisita = new JButton("Registrar Nueva Visita");
        JButton btnCambiarEstado = new JButton("Cambiar Estado");

        btnRegistrarVisita.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarVisita();
            }
        });

        btnCambiarEstado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarEstado();
            }
        });

        panelBotones.add(btnRegistrarVisita);
        panelBotones.add(btnCambiarEstado);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tablaVisitas.getModel();
        model.setRowCount(0);

        OrdenRetiroDTO orden = api.obtenerOrdenDeRetiroDetalle(idOrden);
        if (orden != null) {
            List<VisitaDTO> visitas = orden.getVisitas();
            for (VisitaDTO visita : visitas) {
                model.addRow(new Object[] {visita.getFechaDeVisita(), visita.getObservacion(), visita.getBienesRetirados()});
            }
        }
    }

    private void registrarVisita() {
        try {
            String fecha = JOptionPane.showInputDialog(this, "Ingrese la fecha de la visita (dd/MM/yyyy):");
            String observacion = JOptionPane.showInputDialog(this, "Ingrese una observación:");
            String bienes = JOptionPane.showInputDialog(this, "Ingrese los bienes separados por comas:");

            VisitaDTO visita = new VisitaDTO(
                new java.text.SimpleDateFormat("dd/MM/yyyy").parse(fecha),
                observacion,
                List.of(bienes.split(","))
            );

            api.registrarVisita(idOrden, visita);
            cargarDatos();
            JOptionPane.showMessageDialog(this, "Visita registrada con éxito.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado() {
        try {
            String nuevoEstado = JOptionPane.showInputDialog(this, "Ingrese el nuevo estado (1: PENDIENTE, 2: EN_EJECUCION, 3: COMPLETADO):");
            api.actualizarEstadoOrdenRetiro(idOrden, Integer.parseInt(nuevoEstado));
            JOptionPane.showMessageDialog(this, "Estado actualizado con éxito.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cambiar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}