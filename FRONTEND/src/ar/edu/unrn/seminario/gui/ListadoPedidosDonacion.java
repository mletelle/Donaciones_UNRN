package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class ListadoPedidosDonacion extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public ListadoPedidosDonacion(IApi api) {
        setTitle("Listado de Pedidos de Donación");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 400);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);

        JButton btnGenerarOrden = new JButton("Generar Orden de Retiro");
        panel.add(btnGenerarOrden);
 
        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Fecha", "Tipo Vehículo", "Observaciones", "Donante" });
        table = new JTable(tableModel);
        scrollPane.setViewportView(table);

        cargarPedidos(api);

        btnGenerarOrden.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un pedido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int idPedido = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    api.generarOrdenRetiro(idPedido);
                    JOptionPane.showMessageDialog(null, "Orden de retiro generada con éxito.");
                    cargarPedidos(api);
                } catch (ObjetoNuloException | ReglaNegocioException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void cargarPedidos(IApi api) {
        tableModel.setRowCount(0);
        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosPendientes();
        for (PedidoDonacionDTO pedido : pedidos) {
            tableModel.addRow(new Object[] { pedido.getId(), pedido.getFecha(), pedido.getTipoVehiculo(), pedido.getObservaciones(), pedido.getDonanteId() });
        }
    }
}