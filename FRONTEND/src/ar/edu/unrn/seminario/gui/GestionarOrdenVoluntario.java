package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;

public class GestionarOrdenVoluntario extends JFrame {

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private int idOrden;
    private ListadoOrdenesAsignadasVoluntario ventanaPadre; // referencia a la ventana padre

    public GestionarOrdenVoluntario(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Gestionar Orden de Retiro");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        modeloTabla = new DefaultTableModel(new Object[]{"ID Pedido", "Donante", "Direccion", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ninguna celda es editable
            }
        };

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
                    // pasar como referencia para que el dialogo pueda notificar cuando se guarde
                    RegistrarVisitaDialog registrarVisitaDialog = new RegistrarVisitaDialog(api, idOrden, idPedido, GestionarOrdenVoluntario.this);
                    registrarVisitaDialog.setLocationRelativeTo(GestionarOrdenVoluntario.this);
                    registrarVisitaDialog.setVisible(true);
                    // el dialogo ya llamara a recargarDatos() cuando se guarde exitosamente
                } else {
                    JOptionPane.showMessageDialog(GestionarOrdenVoluntario.this, "Seleccione un pedido para registrar la visita.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelPrincipal.add(btnRegistrarVisita, BorderLayout.SOUTH);

        add(panelPrincipal);
        cargarPedidos();
    }
    
    // constructor que recibe la ventana padre para notificarla
    public GestionarOrdenVoluntario(IApi api, int idOrden, ListadoOrdenesAsignadasVoluntario ventanaPadre) {
        this(api, idOrden);
        this.ventanaPadre = ventanaPadre;
    }

    private void cargarPedidos() {
        // limpiar la tabla antes de cargar
        modeloTabla.setRowCount(0);

        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
        for (PedidoDonacionDTO pedido : pedidos) {
            modeloTabla.addRow(new Object[]{pedido.getId(), pedido.getDonante(), pedido.getDireccion(), pedido.getEstado()});
        }
    }
    
    //  para recargar los datos desde el dialogo hijo
    public void recargarDatos() {
        cargarPedidos();
        tablaPedidos.repaint();
        tablaPedidos.revalidate();
        
        // si hay una ventana padre, tambien la recargamos asi actualiza
        if (ventanaPadre != null) {
            ventanaPadre.refrescarTabla();
        }
    }
}
