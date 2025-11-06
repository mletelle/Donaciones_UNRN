package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import ar.edu.unrn.seminario.dto.BienDTO;
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
        formPanel.add(new JLabel("Categoría:"), gbc);

        gbc.gridx = 1;
        categoriaComboBox = new JComboBox<>(new String[] {
            "Ropa", "Muebles", "Alimentos", "Electrodomésticos", "Herramientas",
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

        // Descripcin
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Descripción:"), gbc);

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

        categoriaComboBox.addActionListener(event -> {
            String categoria = (String) categoriaComboBox.getSelectedItem();
            if ("Alimentos".equals(categoria)) {
                fechaVencimientoTextField.setEnabled(true);
            } else {
                fechaVencimientoTextField.setEnabled(false);
                fechaVencimientoTextField.setText("");
            }
        });

        aceptarButton.addActionListener(event -> {
            try {
                String categoria = (String) categoriaComboBox.getSelectedItem();
                int cantidad = Integer.parseInt(cantidadTextField.getText());
                String estado = (String) estadoComboBox.getSelectedItem();

                int categoriaId = mapCategoriaToId(categoria);
                int estadoId = "Nuevo".equals(estado) ? BienDTO.TIPO_NUEVO : BienDTO.TIPO_USADO;

                LocalDate fechaVencimiento = null;
                if (fechaVencimientoTextField.isEnabled() && !fechaVencimientoTextField.getText().isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaVencimiento = LocalDate.parse(fechaVencimientoTextField.getText(), formatter);

                    if (!fechaVencimiento.isAfter(LocalDate.now())) {
                        throw new FechaVencimientoInvalidaException("La fecha de vencimiento debe ser posterior a la fecha actual.");
                    }
                }

                bien = new BienDTO(estadoId, cantidad, categoriaId, descripcionTextField.getText(), fechaVencimiento);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AgregarBienDialog.this, "Cantidad debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(AgregarBienDialog.this, "Fecha de vencimiento inválida.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (FechaVencimientoInvalidaException ex) {
                JOptionPane.showMessageDialog(AgregarBienDialog.this, ex.getMessage(), "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(AgregarBienDialog.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelarButton.addActionListener(event -> dispose());
    }

    private int mapCategoriaToId(String categoria) {
        switch (categoria) {
            case "Ropa": return BienDTO.CATEGORIA_ROPA;
            case "Muebles": return BienDTO.CATEGORIA_MUEBLES;
            case "Alimentos": return BienDTO.CATEGORIA_ALIMENTOS;
            case "Electrodomésticos": return BienDTO.CATEGORIA_ELECTRODOMESTICOS;
            case "Herramientas": return BienDTO.CATEGORIA_HERRAMIENTAS;
            case "Juguetes": return BienDTO.CATEGORIA_JUGUETES;
            case "Libros": return BienDTO.CATEGORIA_LIBROS;
            case "Medicamentos": return BienDTO.CATEGORIA_MEDICAMENTOS;
            case "Higiene": return BienDTO.CATEGORIA_HIGIENE;
            default: return BienDTO.CATEGORIA_OTROS;
        }
    }

    public BienDTO getBien() {
        return bien;
    }
}