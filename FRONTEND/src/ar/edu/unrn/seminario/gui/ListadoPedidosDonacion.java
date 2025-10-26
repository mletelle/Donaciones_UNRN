package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;

public class ListadoPedidosDonacion extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public ListadoPedidosDonacion(IApi api) {
        setTitle("Listado de Pedidos de Donación");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 400);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Fecha", "Tipo Vehículo", "Observaciones", "Donante" });
        table = new JTable(tableModel);
        scrollPane.setViewportView(table);

        cargarPedidos(api);
    }

    private void cargarPedidos(IApi api) {
        tableModel.setRowCount(0);
        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosPendientes();
        for (PedidoDonacionDTO pedido : pedidos) {
            tableModel.addRow(new Object[] { pedido.getId(), pedido.getFecha(), pedido.getTipoVehiculo(), pedido.getObservaciones(), pedido.getDonanteId() });
        }
    }
}