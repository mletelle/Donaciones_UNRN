package ar.edu.unrn.seminario.gui;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JList;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.DonanteDTO;

public class RegistrarPedidoDonacion extends JDialog {

    private JPanel contentPane;
    private JTextField fechaTextField;
    private JComboBox<DonanteDTO> donanteComboBox;
    private JComboBox<String> tipoVehiculoComboBox;
    private List<BienDTO> bienes;
    private JTable bienesTable;
    private int donanteId; 

    public RegistrarPedidoDonacion(IApi api) {
        setTitle("Registrar Pedido de Donación");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 350);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());

        JLabel lblFecha = new JLabel("Fecha:");
        contentPane.add(lblFecha, BorderLayout.NORTH);

        fechaTextField = new JTextField();
        contentPane.add(fechaTextField, BorderLayout.NORTH);
        fechaTextField.setColumns(10);

        JLabel lblTipoVehiculo = new JLabel("Tipo de Vehículo:");
        contentPane.add(lblTipoVehiculo, BorderLayout.NORTH);

        tipoVehiculoComboBox = new JComboBox<>();
        tipoVehiculoComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"Auto", "Camioneta", "Camión"}));
        contentPane.add(tipoVehiculoComboBox, BorderLayout.NORTH);


        JLabel lblDonante = new JLabel("Donante:");
        contentPane.add(lblDonante, BorderLayout.NORTH);

        donanteComboBox = new JComboBox<>();
        contentPane.add(donanteComboBox, BorderLayout.NORTH);

        JButton btnAgregarBien = new JButton("Agregar Bien");
        contentPane.add(btnAgregarBien, BorderLayout.NORTH);

        JButton btnAceptar = new JButton("Cargar Pedido de Donación");
        contentPane.add(btnAceptar, BorderLayout.SOUTH);

        JButton btnCancelar = new JButton("Cancelar");
        contentPane.add(btnCancelar, BorderLayout.SOUTH);

        bienes = new ArrayList<>();

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaTextField.setText(fechaActual.format(formatter));

        btnAgregarBien.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AgregarBienDialog dialog = new AgregarBienDialog(RegistrarPedidoDonacion.this);
                dialog.setVisible(true);
                BienDTO bien = dialog.getBien();
                if (bien != null) {
                    bienes.add(bien);
                    if (bienesTable.getModel() instanceof BienTableModel) {
                        ((BienTableModel) bienesTable.getModel()).fireTableDataChanged();
                    }
                    JOptionPane.showMessageDialog(RegistrarPedidoDonacion.this, "Bien agregado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String fechaStr = fechaTextField.getText();
                    String tipoVehiculo = (String) tipoVehiculoComboBox.getSelectedItem();
                    int idDonanteSeleccionado;

                    if (!donanteComboBox.isEnabled()) {
                        idDonanteSeleccionado = donanteId; 
                    } else {
                        DonanteDTO donanteSeleccionado = (DonanteDTO) donanteComboBox.getSelectedItem();
                        idDonanteSeleccionado = donanteSeleccionado.getId();
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fechaParsed = LocalDate.parse(fechaStr, formatter);
                    LocalDateTime fechaCompleta = fechaParsed.atStartOfDay();

                    // Formatear LocalDateTime de vuelta a String para el DTO
                    String fechaFormateadaParaDTO = fechaCompleta.format(formatter);

                    PedidoDonacionDTO pedido = new PedidoDonacionDTO(fechaFormateadaParaDTO, bienes, tipoVehiculo, "", idDonanteSeleccionado);
                    api.registrarPedidoDonacion(pedido);

                    JOptionPane.showMessageDialog(null, "Pedido registrado con éxito.");
                    dispose();
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Use dd/MM/yyyy.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Ocurrió un error al registrar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Cargar donantes en el combo box
        List<DonanteDTO> donantes = api.obtenerDonantes();
        for (DonanteDTO donante : donantes) {
            donanteComboBox.addItem(donante);
        }

        donanteComboBox.setRenderer(new javax.swing.ListCellRenderer<DonanteDTO>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends DonanteDTO> list, DonanteDTO value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel();
                if (value != null) {
                    label.setText(value.getNombre() + " " + value.getApellido());
                }
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    label.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }
                label.setOpaque(true);
                return label;
            }
        });

        bienesTable = new JTable(new BienTableModel(bienes));
        JScrollPane scrollPane = new JScrollPane(bienesTable);
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 10));
        panelFormulario.add(new JLabel("Fecha:"));
        panelFormulario.add(fechaTextField);
        panelFormulario.add(new JLabel("Tipo de Vehículo:"));
        panelFormulario.add(tipoVehiculoComboBox);
        panelFormulario.add(new JLabel("Donante:"));
        panelFormulario.add(donanteComboBox);
        panelFormulario.add(new JLabel("")); // 
        panelFormulario.add(btnAgregarBien);
        contentPane.add(panelFormulario, BorderLayout.NORTH);

        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesAccion.add(btnAceptar);
        panelBotonesAccion.add(btnCancelar);
        contentPane.add(panelBotonesAccion, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 400));
        pack();
    }

    public RegistrarPedidoDonacion(IApi api, int donanteId) {
        this(api); //
        this.donanteId = donanteId;

        if (this.donanteId != -1) { // 
            donanteComboBox.setEnabled(false);
            for (int i = 0; i < donanteComboBox.getItemCount(); i++) {
                DonanteDTO donante = donanteComboBox.getItemAt(i);
                if (donante.getId() == this.donanteId) {
                    donanteComboBox.setSelectedItem(donante);
                    break;
                }
            }
        } else {
            donanteComboBox.setEnabled(true);
        }
    }
}