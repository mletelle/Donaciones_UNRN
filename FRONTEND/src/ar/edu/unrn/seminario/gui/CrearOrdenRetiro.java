package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;

public class CrearOrdenRetiro extends JDialog {

    private IApi api;
    private JTable pedidosTable;
    private DefaultTableModel pedidosTableModel;
    private JComboBox<VoluntarioDTO> voluntarioComboBox;
    private JComboBox<String> tipoVehiculoComboBox;


    public CrearOrdenRetiro(Window owner, IApi api) {
        super(owner, "Crear Orden de Retiro", ModalityType.APPLICATION_MODAL);
        this.api = api;

        setLayout(new BorderLayout());

        // Table setup
        pedidosTableModel = new DefaultTableModel(new Object[][] {}, new String[] { "Seleccionar", "ID", "Fecha", "Donante ID", "Tipo Vehículo" }) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        pedidosTable = new JTable(pedidosTableModel);
        add(new JScrollPane(pedidosTable), BorderLayout.CENTER);

        cargarPedidosPendientes();

        // Assignment panel setup
        JPanel panelAsignacion = new JPanel(new FlowLayout());
        panelAsignacion.add(new JLabel("Voluntario:"));

        voluntarioComboBox = new JComboBox<>();
        cargarVoluntarios();
        panelAsignacion.add(voluntarioComboBox);

        panelAsignacion.add(new JLabel("Vehículo:"));
        tipoVehiculoComboBox = new JComboBox<>(new String[] { "Auto", "Camioneta", "Camión" });
        panelAsignacion.add(tipoVehiculoComboBox);

        JButton btnAsignarCrearOrden = new JButton("Asignar y Crear");
        panelAsignacion.add(btnAsignarCrearOrden);

        JButton btnCancelar = new JButton("Cancelar");
        panelAsignacion.add(btnCancelar);

        add(panelAsignacion, BorderLayout.SOUTH);

        // Button actions
        btnAsignarCrearOrden.addActionListener(e -> asignarYCrearOrden());
        btnCancelar.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    private void cargarPedidosPendientes() {
        pedidosTableModel.setRowCount(0);
        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosPendientes();
        for (PedidoDonacionDTO pedido : pedidos) {
            pedidosTableModel.addRow(new Object[] { false, pedido.getId(), pedido.getFecha(), pedido.getDonanteId(), pedido.getTipoVehiculo() });
        }
    }

    private void cargarVoluntarios() {
        List<VoluntarioDTO> voluntarios = api.obtenerVoluntarios();
        for (VoluntarioDTO voluntario : voluntarios) {
            voluntarioComboBox.addItem(voluntario);
        }
        voluntarioComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof VoluntarioDTO) {
                    VoluntarioDTO voluntario = (VoluntarioDTO) value;
                    setText(voluntario.getNombre() + " " + voluntario.getApellido());
                }
                return this;
            }
        });
    }

    private void asignarYCrearOrden() {
        List<Integer> idsPedidosSeleccionados = new ArrayList<>();

        for (int i = 0; i < pedidosTableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) pedidosTableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                idsPedidosSeleccionados.add((Integer) pedidosTableModel.getValueAt(i, 1));
            }
        }

        if (idsPedidosSeleccionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        VoluntarioDTO voluntarioSeleccionado = (VoluntarioDTO) voluntarioComboBox.getSelectedItem();
        if (voluntarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un voluntario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tipoVehiculoSeleccionado = (String) tipoVehiculoComboBox.getSelectedItem();

        try {
            api.crearOrdenRetiro(idsPedidosSeleccionados, voluntarioSeleccionado.getId(), tipoVehiculoSeleccionado);
            JOptionPane.showMessageDialog(this, "Orden creada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPedidosPendientes();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}