package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.PedidoDonacionDTO;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException;


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
        
        // Accion del boton "Registrar Visita"
        btnRegistrarVisita.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int filaSeleccionada = tablaPedidos.getSelectedRow();
                    
                    // --- USO DE CAMPOVACIOEXCEPTION (Validar selección de fila) ---
                    if (filaSeleccionada == -1) {
                        throw new CampoVacioException("Seleccione un pedido de la lista para registrar la visita.");
                    }
                    // ------------------------------------------------------------------
                    
                    int idPedido = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    
                    // Abrir el diálogo de registro de visita
                    RegistrarVisitaDialog registrarVisitaDialog = new RegistrarVisitaDialog(api, idOrden, idPedido, GestionarOrdenVoluntario.this);
                    registrarVisitaDialog.setLocationRelativeTo(GestionarOrdenVoluntario.this);
                    registrarVisitaDialog.setVisible(true);
                    
                } catch (CampoVacioException ex) {
                    // Manejo de la excepción custom para falta de selección
                    JOptionPane.showMessageDialog(GestionarOrdenVoluntario.this, ex.getMessage(), "Error de Selección", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    // Manejo de cualquier otro error inesperado
                    JOptionPane.showMessageDialog(GestionarOrdenVoluntario.this, "Error al procesar la selección: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    // Metodos
    // metodo para cargar pedidos
    private void cargarPedidos() {
        // limpiar la tabla antes de cargar
        modeloTabla.setRowCount(0);

        try {
            List<PedidoDonacionDTO> pedidos = api.obtenerPedidosDeOrden(idOrden);
            
            // --- USO DE OBJETONULOEXCEPTION (Si la API retorna null) ---
            if (pedidos == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de pedidos.");
            }
            // -----------------------------------------------------------
            
            for (PedidoDonacionDTO pedido : pedidos) {
                modeloTabla.addRow(new Object[]{pedido.getId(), pedido.getDonante(), pedido.getDireccion(), pedido.getEstado()});
            }
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Datos", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
             // Manejo de otros errores de la API
            JOptionPane.showMessageDialog(this, "Error al obtener pedidos: " + ex.getMessage(), "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // metodo para recargar los datos desde el dialogo hijo
    public void recargarDatos() {
        cargarPedidos();
        tablaPedidos.repaint();
        tablaPedidos.revalidate();
        
        // si hay una ventana padre, tambien la recargamos asi actualiza
        // Asumiendo que ListadoOrdenesAsignadasVoluntario tiene el método refrescarTabla()
        if (ventanaPadre != null) {
            ventanaPadre.refrescarTabla();
        }
    }
    
}