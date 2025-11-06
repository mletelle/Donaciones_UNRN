package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn; // 
import javax.swing.event.TableModelEvent; // 
import javax.swing.event.TableModelListener; // 
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

        modeloTabla = new DefaultTableModel(new Object[]{"ID Pedido", "Donante", "Dirección", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        tablaPedidos = new JTable(modeloTabla);

        // ComboBox como editor para la columna Estado
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
        // poner la bandera en true para que el listener no se dispare mientras se cargan los datos iniciales.
        this.actualizandoDatos = true;
        
        // limpamos la tabla antes de cargar
        modeloTabla.setRowCount(0); 

        List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
        for (PedidoDonacionDTO pedido : pedidos) {
            modeloTabla.addRow(new Object[]{pedido.getId(), pedido.getDonante(), pedido.getDireccion(), pedido.getEstado()});
        }
        
        // quita la bandera
        this.actualizandoDatos = false;
    }

    private void configurarEditorDeEstado() {
        JComboBox<String> comboBoxEstados = new JComboBox<>(this.estadosValidos);
        
        // obtener estado
        TableColumn estadoColumn = tablaPedidos.getColumnModel().getColumn(3);
        
        // 
        estadoColumn.setCellEditor(new DefaultCellEditor(comboBoxEstados));
    }


    private void actualizarEstadoDelPedido(int idPedido, String nuevoEstado) {
        try {
            api.actualizarEstadoDelPedido(idPedido, nuevoEstado); 
            
            JOptionPane.showMessageDialog(this, "Estado del pedido " + idPedido + " actualizado a " + nuevoEstado + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            cargarPedidos(); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado al actualizar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            cargarPedidos(); 
        }
    }
}
