package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class RegistrarVisitaDialog extends JDialog {

    private JTextField txtFecha;
    private JTextField txtHora;
    private JComboBox<String> cmbResultado;
    private JTextArea txtObservaciones;
    private IApi api;
    private int idOrden;
    private int idPedido;

    public RegistrarVisitaDialog(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Registrar Visita");
        setSize(400, 300);
        setModal(true);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        JPanel panelCampos = new JPanel(new GridLayout(4, 2));
        panelCampos.add(new JLabel("Fecha (YYYY-MM-DD):"));
        txtFecha = new JTextField();
        panelCampos.add(txtFecha);

        panelCampos.add(new JLabel("Hora (HH:MM):"));
        txtHora = new JTextField();
        panelCampos.add(txtHora);

        panelCampos.add(new JLabel("Resultado:"));
        cmbResultado = new JComboBox<>(new String[]{"Recolección Exitosa", "Donante Ausente", "Recolección Parcial", "Cancelado"});
        panelCampos.add(cmbResultado);

        panelCampos.add(new JLabel("Observaciones:"));
        txtObservaciones = new JTextArea(3, 20);
        panelCampos.add(new JScrollPane(txtObservaciones));

        panelCampos.add(new JLabel("ID Pedido:"));
        JTextField txtIdPedido = new JTextField(String.valueOf(idPedido));
        txtIdPedido.setEditable(false);
        panelCampos.add(txtIdPedido);

        panelPrincipal.add(panelCampos, BorderLayout.CENTER);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarVisita();
            }
        });
        panelPrincipal.add(btnGuardar, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    public RegistrarVisitaDialog(IApi api, int idOrden, int idPedido) {
        this(api, idOrden);
        this.idPedido = idPedido;
    }

    private void guardarVisita() {
        try {
            String fecha = txtFecha.getText();
            String hora = txtHora.getText();
            String resultado = (String) cmbResultado.getSelectedItem();
            String observaciones = txtObservaciones.getText();

            LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + hora);
            VisitaDTO visita = new VisitaDTO(fechaHora, resultado, observaciones);

            api.registrarVisita(idOrden, visita);
            JOptionPane.showMessageDialog(this, "Visita registrada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la visita: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}