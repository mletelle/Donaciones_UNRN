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
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException; 


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

        pedidosTableModel = new DefaultTableModel(new Object[][] {}, new String[] { "Seleccionar", "ID", "Fecha", "Donante", "Tipo Vehiculo" }) {
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

        JPanel panelAsignacion = new JPanel(new FlowLayout());
        panelAsignacion.add(new JLabel("Voluntario:"));

        voluntarioComboBox = new JComboBox<>();
        cargarVoluntarios();
        panelAsignacion.add(voluntarioComboBox);

        panelAsignacion.add(new JLabel("Vehiculo:"));
        tipoVehiculoComboBox = new JComboBox<>(new String[] { "Auto", "Camioneta", "Camion" });
        panelAsignacion.add(tipoVehiculoComboBox);

        JButton btnAsignarCrearOrden = new JButton("Asignar y Crear");
        panelAsignacion.add(btnAsignarCrearOrden);

        JButton btnCancelar = new JButton("Cancelar");
        panelAsignacion.add(btnCancelar);

        add(panelAsignacion, BorderLayout.SOUTH);

        // Agregamos el manejo de excepciones personalizado al listener
        btnAsignarCrearOrden.addActionListener(e -> asignarYCrearOrden());
        btnCancelar.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    // Metodos
    // Metodo para cargar pedidos pendientes
    private void cargarPedidosPendientes() {
        pedidosTableModel.setRowCount(0);
        // Podríamos usar ObjetoNuloException si api.obtenerPedidosPendientes() retorna null
        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosPendientes();
        if (pedidos != null) {
            for (PedidoDonacionDTO pedido : pedidos) {
                pedidosTableModel.addRow(new Object[] { false, pedido.getId(), pedido.getFecha(), pedido.getDonante(), pedido.getTipoVehiculo() });
            }
        }
    }

    // Metodo para cargar voluntarios
    private void cargarVoluntarios() {
        // Podríamos usar ObjetoNuloException si api.obtenerVoluntarios() retorna null
        List<VoluntarioDTO> voluntarios = api.obtenerVoluntarios();
        if (voluntarios != null) {
            for (VoluntarioDTO voluntario : voluntarios) {
                voluntarioComboBox.addItem(voluntario);
            }
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

    // Metodo para asignar y crear una orden de retiro
    private void asignarYCrearOrden() {
        List<Integer> idsPedidosSeleccionados = new ArrayList<>();

        try {
            for (int i = 0; i < pedidosTableModel.getRowCount(); i++) {
                Boolean isSelected = (Boolean) pedidosTableModel.getValueAt(i, 0);
                if (isSelected != null && isSelected) {
                    // Se asume que la columna 1 (ID) es un Integer
                    idsPedidosSeleccionados.add((Integer) pedidosTableModel.getValueAt(i, 1));
                }
            }

            // --- USO DE CAMPOVACIOEXCEPTION (Para validar selección en la tabla) ---
            if (idsPedidosSeleccionados.isEmpty()) {
                throw new CampoVacioException("Debe seleccionar al menos un pedido para crear la orden de retiro.");
            }
            // ----------------------------------------------------------------------

            VoluntarioDTO voluntarioSeleccionado = (VoluntarioDTO) voluntarioComboBox.getSelectedItem();

            // --- USO DE OBJETONULOEXCEPTION (Para validar la selección del JComboBox) ---
            if (voluntarioSeleccionado == null) {
                throw new ObjetoNuloException("Debe seleccionar un voluntario para asignar la orden.");
            }
            // --------------------------------------------------------------------------

            String tipoVehiculoSeleccionado = (String) tipoVehiculoComboBox.getSelectedItem();

            // Llamada a la API
            api.crearOrdenRetiro(idsPedidosSeleccionados, voluntarioSeleccionado.getId(), tipoVehiculoSeleccionado);
            
            JOptionPane.showMessageDialog(this, "Orden creada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Recargar datos y cerrar
            cargarPedidosPendientes();
            dispose();
            
        } catch (CampoVacioException | ObjetoNuloException ex) {
            // Manejo de tus excepciones custom
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Asignación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            // Manejo de cualquier otra excepción (ej. errores lanzados por la API, como duplicados, etc.)
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error de Creación de Orden", JOptionPane.ERROR_MESSAGE);
        }
    }
}