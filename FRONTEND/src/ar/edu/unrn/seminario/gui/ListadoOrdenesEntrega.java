package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.OrdenEntregaDTO;

public class ListadoOrdenesEntrega extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel tableModel;
    private IApi api;

    public ListadoOrdenesEntrega(IApi api) {
        this.api = api;
        setTitle("Listado de Órdenes de Entrega");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 900, 500);
        getContentPane().setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
                new String[] { "ID", "Fecha", "Estado", "Beneficiario", "Voluntario" },
                0
        );
        table = new JTable(tableModel);
        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        btnPanel.add(btnCerrar);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        cargarOrdenes();
    }

    private void cargarOrdenes() {
        tableModel.setRowCount(0);
        List<OrdenEntregaDTO> ordenes = api.obtenerTodasOrdenesEntrega();
        
        if (ordenes != null) {
            for (OrdenEntregaDTO orden : ordenes) {
                tableModel.addRow(new Object[] {
                        orden.getId(),
                        orden.getFecha(),
                        orden.getEstado(),
                        orden.getBeneficiario(),
                        orden.getVoluntario()
                });
            }
        }
    }
}
