package ar.edu.unrn.seminario.gui;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException; // Importar para manejar errores de formato de fecha

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
import ar.edu.unrn.seminario.dto.UsuarioDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException; 

public class RegistrarPedidoDonacion extends JDialog {

    private JPanel contentPane;
    private JTextField fechaTextField;
    private JComboBox<UsuarioDTO> donanteComboBox;
    private JComboBox<String> tipoVehiculoComboBox;
    private List<BienDTO> bienes;
    private JTable bienesTable;
    private int donanteId; 
    private IApi api; // Se añade la referencia a la API como atributo de instancia

    public RegistrarPedidoDonacion(IApi api) {
        this.api = api; // Asignar la API
        bienes = new ArrayList<>(); // Inicializar la lista de bienes

        setTitle("Registrar Pedido de Donacion");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 350);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());

        // Inicialización de componentes (mejorados para el layout)
        fechaTextField = new JTextField();
        tipoVehiculoComboBox = new JComboBox<>();
        tipoVehiculoComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"Auto", "Camioneta", "Camion"}));
        donanteComboBox = new JComboBox<>();
        
        JButton btnAgregarBien = new JButton("Agregar Bien");
        JButton btnAceptar = new JButton("Cargar Pedido de Donacion");
        JButton btnCancelar = new JButton("Cancelar");

        // Inicializar fecha
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        fechaTextField.setText(fechaActual.format(formatter));

        // Carga de donantes y manejo de errores (método refactorizado)
        cargarDonantes();

        // Configuración de la tabla de bienes
        bienesTable = new JTable(new BienTableModel(bienes));
        JScrollPane scrollPane = new JScrollPane(bienesTable);
        
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 10));
        panelFormulario.add(new JLabel("Fecha (dd/MM/yyyy):"));
        panelFormulario.add(fechaTextField);
        panelFormulario.add(new JLabel("Tipo de Vehiculo:"));
        panelFormulario.add(tipoVehiculoComboBox);
        panelFormulario.add(new JLabel("Donante:"));
        panelFormulario.add(donanteComboBox);
        panelFormulario.add(new JLabel("")); 
        panelFormulario.add(btnAgregarBien);
        contentPane.add(panelFormulario, BorderLayout.NORTH);

        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesAccion.add(btnAceptar);
        panelBotonesAccion.add(btnCancelar);
        contentPane.add(panelBotonesAccion, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 400));
        pack();
        
        btnAgregarBien.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AgregarBienDialog dialog = new AgregarBienDialog(RegistrarPedidoDonacion.this);
                dialog.setVisible(true);
                BienDTO bien = dialog.getBien();
                if (bien != null) {
                    bienes.add(bien);
                    if (bienesTable.getModel() instanceof BienTableModel) {
                        ((BienTableModel) bienesTable.getModel()).setBienes(bienes);
                    }
                    JOptionPane.showMessageDialog(RegistrarPedidoDonacion.this, "Bien agregado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (bienes.isEmpty()) {
                        throw new CampoVacioException("Debe agregar al menos un bien a la donación para registrar el pedido.");
                    }
                    
                    String fechaStr = fechaTextField.getText();
                    String tipoVehiculo = (String) tipoVehiculoComboBox.getSelectedItem();
                    int idDonanteSeleccionado;

                    if (donanteComboBox.isEnabled()) {
                        UsuarioDTO donanteSeleccionado = (UsuarioDTO) donanteComboBox.getSelectedItem();
                        
                        if (donanteSeleccionado == null) {
                            throw new ObjetoNuloException("Debe seleccionar un donante de la lista.");
                        }
                        idDonanteSeleccionado = donanteSeleccionado.getId();
                    } else {
                        if (donanteId == -1) {
                             throw new ObjetoNuloException("Error interno: No se pudo determinar el ID del donante precargado.");
                        }
                        idDonanteSeleccionado = donanteId;
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fechaParsed;
                    try {
                        fechaParsed = LocalDate.parse(fechaStr, formatter);
                    } catch (DateTimeParseException ex) {
                        throw new DateTimeParseException("Formato de fecha inválido. Use dd/MM/yyyy.", fechaStr, 0);
                    }
                    
                    if (fechaParsed.isBefore(LocalDate.now())) {
                        throw new ReglaNegocioException("La fecha del pedido no puede ser anterior a la fecha actual.");
                    }
                    
                    // Se usa la fecha completa (LocalDate + hora inicial)
                    LocalDateTime fechaCompleta = fechaParsed.atStartOfDay();
                    String fechaFormateadaParaDTO = fechaCompleta.format(formatter);

                    PedidoDonacionDTO pedido = new PedidoDonacionDTO(fechaFormateadaParaDTO, bienes, tipoVehiculo, idDonanteSeleccionado);
                    api.registrarPedidoDonacion(pedido);

                    JOptionPane.showMessageDialog(null, "Pedido registrado con éxito.");
                    dispose();
                    
                } catch (CampoVacioException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
                } catch (ObjetoNuloException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Selección", JOptionPane.WARNING_MESSAGE);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(null, "Formato de fecha inválido. Use dd/MM/yyyy.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                } catch (ReglaNegocioException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Regla de Negocio", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Accion del boton cancelar
        btnCancelar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Renderizador para UsuarioDTO (para que muestre Nombre Apellido)
        donanteComboBox.setRenderer(new javax.swing.ListCellRenderer<UsuarioDTO>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends UsuarioDTO> list, UsuarioDTO value, int index, boolean isSelected, boolean cellHasFocus) {
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
    }

    public RegistrarPedidoDonacion(IApi api, int donanteId) {
        this(api);
        this.donanteId = donanteId;

        if (this.donanteId != -1) {
            donanteComboBox.setEnabled(false);
            for (int i = 0; i < donanteComboBox.getItemCount(); i++) {
                UsuarioDTO donante = donanteComboBox.getItemAt(i);
                if (donante.getId() == this.donanteId) {
                    donanteComboBox.setSelectedItem(donante);
                    break;
                }
            }
        } else {
            donanteComboBox.setEnabled(true);
        }
    }
    
    // Método refactorizado para la carga de donantes
    private void cargarDonantes() {
        try {
            List<UsuarioDTO> donantes = api.obtenerDonantes();
            
            if (donantes == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de donantes.");
            }
            
            for (UsuarioDTO donante : donantes) {
                donanteComboBox.addItem(donante);
            }
            
            if (donantes.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "No hay donantes registrados para seleccionar.", "Advertencia", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (ObjetoNuloException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
            donanteComboBox.setEnabled(false);
        }
    }
}