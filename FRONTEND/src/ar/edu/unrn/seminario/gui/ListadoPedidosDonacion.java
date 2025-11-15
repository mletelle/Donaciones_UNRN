package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane; // Importar para mostrar mensajes de error
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.ObjetoNuloException; 

public class ListadoPedidosDonacion extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public ListadoPedidosDonacion(IApi api) {
        setTitle("Listado de Pedidos de Donacion");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 700, 400);

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[][] {}, new String[] { "ID", "Fecha", "Tipo Vehiculo", "Donante", "Estado" });
        table = new JTable(tableModel);
        scrollPane.setViewportView(table);

        cargarPedidos(api);
    }

    // Metodos
    // metodo para cargar pedidos
    private void cargarPedidos(IApi api) {
        tableModel.setRowCount(0);
        
        try {
            List<PedidoDonacionDTO> pedidos = api.obtenerTodosPedidos();
            
            // --- USO DE OBJETONULOEXCEPTION (Si la API retorna null) ---
            if (pedidos == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de pedidos de donación.");
            }
            // -----------------------------------------------------------
            
            for (PedidoDonacionDTO pedido : pedidos) {
                // Se asume que getDonante() y getEstado() devuelven datos adecuados para JTable
                tableModel.addRow(new Object[] { pedido.getId(), pedido.getFecha(), pedido.getTipoVehiculo(), pedido.getDonante(), pedido.getEstado() });
            }
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom para datos nulos
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga de Datos", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
             // Manejo de otros errores de la API
            JOptionPane.showMessageDialog(this, "Error al obtener los pedidos: " + ex.getMessage(), "Error de API", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}