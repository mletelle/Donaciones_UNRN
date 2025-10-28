package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn; // --- NUEVO ---
import javax.swing.event.TableModelEvent; // --- NUEVO ---
import javax.swing.event.TableModelListener; // --- NUEVO ---
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.exception.ReglaNegocioException;

public class GestionarOrdenVoluntario extends JFrame {

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private IApi api;
    private int idOrden;
    private String[] estadosValidos = {"PENDIENTE", "EN_EJECUCION", "COMPLETADO"};
    
    // flag, evita llamadas recursivas al listener mientras se actualiza la API
    private boolean actualizandoDatos = false; 

    public GestionarOrdenVoluntario(IApi api, int idOrden) {
        this.api = api;
        this.idOrden = idOrden;

        setTitle("Gestionar Orden de Retiro");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        //r 'isCellEditable' para que solo la columna 3 sea editable
        modeloTabla = new DefaultTableModel(new Object[]{"ID Pedido", "Donante", "Dirección", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo la columna de "Estado" (índice 3) es editable
                return column == 3;
            }
        };

        tablaPedidos = new JTable(modeloTabla);

        // JComboBox como editor para la columna "Estado"
        configurarEditorDeEstado();

        // listener para guardar cambios automáticamente
        modeloTabla.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && !actualizandoDatos) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (column == 3) {
                        int idPedido = (int) modeloTabla.getValueAt(row, 0);
                        String nuevoEstado = (String) modeloTabla.getValueAt(row, 3);
                        actualizarEstadoDelPedido(idPedido, nuevoEstado);
                    }
                }
            }
        });


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
        // Poner la bandera en true para que el listener no se dispare
        // mientras se cargan los datos iniciales.
        this.actualizandoDatos = true;
        
        // Limpiamos la tabla antes de cargar
        modeloTabla.setRowCount(0); 

        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
        for (PedidoDonacionDTO pedido : pedidos) {
            modeloTabla.addRow(new Object[]{pedido.getId(), pedido.getDonante(), pedido.getDireccion(), pedido.getEstado()});
        }
        
        // Quitar la bandera
        this.actualizandoDatos = false;
    }

    /*
     * Asigna un JComboBox a la columna "Estado" de la tabla.
     */
    private void configurarEditorDeEstado() {
        JComboBox<String> comboBoxEstados = new JComboBox<>(this.estadosValidos);
        
        // Obtener el modelo de la columna "Estado" (índice 3)
        TableColumn estadoColumn = tablaPedidos.getColumnModel().getColumn(3);
        
        // Asignar el JComboBox como el editor para esa celda
        estadoColumn.setCellEditor(new DefaultCellEditor(comboBoxEstados));
    }

    /*
     * Llama a la API para persistir el cambio de estado de un pedido.
     */
    private void actualizarEstadoDelPedido(int idPedido, String nuevoEstado) {
        try {
            // Llamada real a la API con el método implementado
            api.actualizarEstadoDelPedido(idPedido, nuevoEstado); 
            
            JOptionPane.showMessageDialog(this, "Estado del pedido " + idPedido + " actualizado a " + nuevoEstado + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (ReglaNegocioException ex) {
            // Captura la excepción de negocio (ej: "Pedido no existe")
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Revertir el cambio visual
            cargarPedidos(); 
        } catch (Exception ex) {
            // Captura otros errores (ej: NullPointerException, si la API falla)
            JOptionPane.showMessageDialog(this, "Error inesperado al actualizar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            cargarPedidos(); 
        }
    }
}
