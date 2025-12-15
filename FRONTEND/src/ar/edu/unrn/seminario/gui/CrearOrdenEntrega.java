package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;

public class CrearOrdenEntrega extends JDialog {

    private IApi api;
    private JComboBox<UsuarioDTO> comboBeneficiarios;
    private JComboBox<UsuarioDTO> comboVoluntarios;
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;

    public CrearOrdenEntrega(Window owner, IApi api) {
        super(owner, "Nueva Orden de Entrega", ModalityType.APPLICATION_MODAL);
        this.api = api;
        
        setSize(900, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel panelTop = new JPanel(new GridLayout(2, 2, 10, 10));
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panelTop.add(new JLabel("Seleccionar Beneficiario:")); 
        comboBeneficiarios = new JComboBox<>();
        cargarBeneficiarios();
        panelTop.add(comboBeneficiarios);

        panelTop.add(new JLabel("Asignar Voluntario (Opcional):"));
        comboVoluntarios = new JComboBox<>();
        cargarVoluntarios();
        panelTop.add(comboVoluntarios);
        
        add(panelTop, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Seleccionar", "ID", "Categoría", "Descripción", "Stock Disp.", "A Entregar"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                if (columnIndex == 4 || columnIndex == 5) return Integer.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 5;
            }
        };
        
        tablaInventario = new JTable(modeloTabla);
        cargarInventario();
        
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCrear = new JButton("Generar Orden");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnCrear.addActionListener(e -> generarOrden());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCrear);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarBeneficiarios() {
        try {
            List<UsuarioDTO> beneficiarios = api.obtenerBeneficiarios();
            for (UsuarioDTO b : beneficiarios) comboBeneficiarios.addItem(b);
            // Renderizador
            comboBeneficiarios.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof UsuarioDTO) {
                        UsuarioDTO u = (UsuarioDTO) value;
                        setText(u.getNombre() + " " + u.getApellido() + " (DNI: " + u.getDni() + ")");
                    }
                    return this;
                }
            });
        } catch (Exception e) {}
    }

    private void cargarVoluntarios() {
        try {
            comboVoluntarios.addItem(null); // Opción vacía
            List<UsuarioDTO> voluntarios = api.obtenerVoluntarios();
            for (UsuarioDTO v : voluntarios) comboVoluntarios.addItem(v);
            
            comboVoluntarios.setRenderer(new DefaultListCellRenderer() { 
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof UsuarioDTO) {
                        UsuarioDTO u = (UsuarioDTO) value;
                        setText(u.getNombre() + " " + u.getApellido());
                    } else {
                        setText("---");
                    }
                    return this;
                }
            });
        } catch (Exception e) {}
    }

    private void cargarInventario() {
        try {
            List<BienDTO> bienes = api.obtenerInventario();
            modeloTabla.setRowCount(0);
            for (BienDTO b : bienes) {
                modeloTabla.addRow(new Object[]{ false, b.getId(), b.getCategoriaTexto(), b.getDescripcion(), b.getCantidad(), 0 });
            }
        } catch (Exception e) {}
    }

    private void generarOrden() {
        try {
            UsuarioDTO beneficiario = (UsuarioDTO) comboBeneficiarios.getSelectedItem();
            if (beneficiario == null) throw new CampoVacioException("Debe seleccionar un beneficiario.");

            UsuarioDTO voluntario = (UsuarioDTO) comboVoluntarios.getSelectedItem();
            String userVoluntario = (voluntario != null) ? voluntario.getUsuario() : null;

            Map<Integer, Integer> bienesYCantidades = new HashMap<>();
            
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                Boolean isSelected = (Boolean) modeloTabla.getValueAt(i, 0);
                if (isSelected != null && isSelected) {
                    Integer idBien = (Integer) modeloTabla.getValueAt(i, 1);
                    Integer stock = (Integer) modeloTabla.getValueAt(i, 4);
                    Object cantObj = modeloTabla.getValueAt(i, 5);
                    int cantidadAEntregar = (cantObj instanceof Integer) ? (Integer) cantObj : Integer.parseInt(cantObj.toString());
                    
                    if (cantidadAEntregar <= 0) throw new CampoVacioException("Cantidad debe ser mayor a 0 para bien ID " + idBien);
                    if (cantidadAEntregar > stock) throw new CampoVacioException("Stock insuficiente para bien ID " + idBien);
                    
                    bienesYCantidades.put(idBien, cantidadAEntregar);
                }
            }

            if (bienesYCantidades.isEmpty()) throw new CampoVacioException("Seleccione al menos un bien.");

            // Llamada a la API
            api.crearOrdenEntrega(beneficiario.getUsuario(), bienesYCantidades, userVoluntario);

            JOptionPane.showMessageDialog(this, "Orden de Entrega creada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (CampoVacioException | ObjetoNuloException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Advertencia", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}