package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;

public class GestionarOrdenVoluntario extends JFrame {

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private int idOrden;

    public GestionarOrdenVoluntario(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Gestionar Orden de Retiro");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        modeloTabla = new DefaultTableModel(new Object[]{"ID Pedido", "Donante", "Dirección", "Estado"}, 0);
        tablaPedidos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JButton btnRegistrarVisita = new JButton("Registrar Visita");
        btnRegistrarVisita.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaPedidos.getSelectedRow();
                if (filaSeleccionada != -1) {
                    int idPedido = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    RegistrarVisitaDialog registrarVisitaDialog = new RegistrarVisitaDialog(api, idOrden, idPedido);
                    registrarVisitaDialog.setLocationRelativeTo(null);
                    registrarVisitaDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(GestionarOrdenVoluntario.this, "Seleccione un pedido para registrar la visita.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelPrincipal.add(btnRegistrarVisita, BorderLayout.SOUTH);

        add(panelPrincipal);
        cargarPedidos();
    }

    private void cargarPedidos() {
        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
        for (PedidoDonacionDTO pedido : pedidos) {
            modeloTabla.addRow(new Object[]{pedido.getId(), pedido.getDonante(), pedido.getDireccion(), pedido.getEstado()});
        }
    }
}