package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import ar.edu.unrn.seminario.dto.BienDTO;
// Importa las 3 excepciones custom
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.FechaVencimientoInvalidaException;

public class AgregarBienDialog extends JDialog {

    private JComboBox<String> categoriaComboBox;
    private JTextField cantidadTextField;
    private JComboBox<String> estadoComboBox;
    private JTextField fechaVencimientoTextField;
    private JButton aceptarButton;
    private JButton cancelarButton;

    private BienDTO bien;
    private JTextField descripcionTextField;

    public AgregarBienDialog(Window owner) {
        super(owner, "Agregar Bien", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(owner);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Categoria
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Categoria:"), gbc);

        gbc.gridx = 1;
        categoriaComboBox = new JComboBox<>(new String[] {
            "Ropa", "Muebles", "Alimentos", "Electrodomesticos", "Herramientas",
            "Juguetes", "Libros", "Medicamentos", "Higiene", "Otros"
        });
        formPanel.add(categoriaComboBox, gbc);

        // Cantidad
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Cantidad:"), gbc);

        gbc.gridx = 1;
        cantidadTextField = new JTextField();
        formPanel.add(cantidadTextField, gbc);

        // Estado
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Estado:"), gbc);

        gbc.gridx = 1;
        estadoComboBox = new JComboBox<>(new String[] {"Nuevo", "Usado"});
        formPanel.add(estadoComboBox, gbc);

        // Fecha de Vencimiento
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Fecha de Vencimiento (dd/MM/yyyy):"), gbc);

        gbc.gridx = 1;
        fechaVencimientoTextField = new JTextField();
        fechaVencimientoTextField.setEnabled(false);
        formPanel.add(fechaVencimientoTextField, gbc);

        // Descripcion
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Descripcion:"), gbc);

        gbc.gridx = 1;
        descripcionTextField = new JTextField();
        formPanel.add(descripcionTextField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        aceptarButton = new JButton("Aceptar");
        cancelarButton = new JButton("Cancelar");
        buttonPanel.add(aceptarButton);
        buttonPanel.add(cancelarButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Accion del comboBox de categoria
        categoriaComboBox.addActionListener(event -> {
            String categoria = (String) categoriaComboBox.getSelectedItem();
            if ("Alimentos".equals(categoria)) {
                fechaVencimientoTextField.setEnabled(true);
            } else {
                fechaVencimientoTextField.setEnabled(false);
                fechaVencimientoTextField.setText("");
            }
        });

        // accion del boton "Aceptar"
        aceptarButton.addActionListener(event -> {
            try {
                String cantidadText = cantidadTextField.getText();
                String descripcion = descripcionTextField.getText();
                String fechaVencimientoText = fechaVencimientoTextField.getText();

                if (cantidadText == null || cantidadText.trim().isEmpty()) {
                    throw new CampoVacioException("El campo Cantidad no puede estar vacío.");
                }

                if (descripcion == null || descripcion.trim().isEmpty()) {
                    throw new CampoVacioException("El campo Descripción no puede estar vacío.");
                }

                // fecha de vencimiento obligatoria solo para alimentos
                if (fechaVencimientoTextField.isEnabled() && (fechaVencimientoText == null || fechaVencimientoText.trim().isEmpty())) {
                    throw new CampoVacioException("La fecha de vencimiento es obligatoria para Alimentos.");
                }
                
                String categoria = (String) categoriaComboBox.getSelectedItem();
                
                int cantidad = Integer.parseInt(cantidadText); 
                String estado = (String) estadoComboBox.getSelectedItem();

                int categoriaId = mapearCategoriaAId(categoria);
                int estadoId = "Nuevo".equals(estado) ? BienDTO.TIPO_NUEVO : BienDTO.TIPO_USADO;

                LocalDate fechaVencimiento = null;
                if (fechaVencimientoTextField.isEnabled() && !fechaVencimientoText.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaVencimiento = LocalDate.parse(fechaVencimientoText, formatter);

                    if (!fechaVencimiento.isAfter(LocalDate.now())) {
                        throw new FechaVencimientoInvalidaException("La fecha de vencimiento debe ser posterior a la fecha actual.");
                    }
                }

                bien = new BienDTO(estadoId, cantidad, categoriaId, descripcion, fechaVencimiento);
                if (bien == null) {
                    throw new ObjetoNuloException("Error interno: No se pudo crear el objeto Bien.");
                }

                dispose();
            } catch (CampoVacioException ex) {
                // Manejo de la excepción custom para campos vacíos.
                JOptionPane.showMessageDialog(AgregarBienDialog.this, ex.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                // Manejo si la 'cantidad' no es un número.
                JOptionPane.showMessageDialog(AgregarBienDialog.this, "Cantidad debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                // Manejo si la fecha tiene un formato incorrecto.
                JOptionPane.showMessageDialog(AgregarBienDialog.this, "Fecha de vencimiento inválida (formato esperado: dd/MM/yyyy).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (FechaVencimientoInvalidaException ex) {
                // Manejo de la excepción custom para fecha lógica.
                JOptionPane.showMessageDialog(AgregarBienDialog.this, ex.getMessage(), "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            } catch (ObjetoNuloException ex) {
                 // Manejo de la excepción custom para objeto nulo 
                JOptionPane.showMessageDialog(AgregarBienDialog.this, ex.getMessage(), "Error de Creación", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Accion del boton cancelar
        cancelarButton.addActionListener(event -> dispose());
    }

    private int mapearCategoriaAId(String categoria) {
        switch (categoria) {
            case "Ropa": return BienDTO.CATEGORIA_ROPA;
            case "Muebles": return BienDTO.CATEGORIA_MUEBLES;
            case "Alimentos": return BienDTO.CATEGORIA_ALIMENTOS;
            case "Electrodomesticos": return BienDTO.CATEGORIA_ELECTRODOMESTICOS;
            case "Herramientas": return BienDTO.CATEGORIA_HERRAMIENTAS;
            case "Juguetes": return BienDTO.CATEGORIA_JUGUETES;
            case "Libros": return BienDTO.CATEGORIA_LIBROS;
            case "Medicamentos": return BienDTO.CATEGORIA_MEDICAMENTOS;
            case "Higiene": return BienDTO.CATEGORIA_HIGIENE;
            default: return BienDTO.CATEGORIA_OTROS;
        }
    }
    
    // Getters
    public BienDTO getBien() {
        return bien;
    }
}