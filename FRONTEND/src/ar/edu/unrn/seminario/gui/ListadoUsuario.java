package ar.edu.unrn.seminario.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import ar.edu.unrn.seminario.api.IApi;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.exception.CampoVacioException;
import ar.edu.unrn.seminario.exception.ObjetoNuloException; 

public class ListadoUsuario extends JFrame {

    private JPanel contentPane;
    private JTable table;
    DefaultTableModel modelo;
    IApi api;
    JButton activarButton;
    JButton desactivarButton;

    public ListadoUsuario(IApi api) {
        this.api = api;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        String[] titulos = { "USUARIO", "NOMBRE", "EMAIL", "ESTADO", "ROL" };

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                habilitarBotones(true);
            }
        });
        
        modelo = new DefaultTableModel(new Object[][] {}, titulos);
        table.setModel(modelo);

        scrollPane.setViewportView(table);

        activarButton = new JButton("Activar");
        
        activarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = table.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int opcionSeleccionada = JOptionPane.showConfirmDialog(null,
                        "¿Está seguro que desea cambiar el estado del Usuario?", "Confirmar cambio de estado.",
                        JOptionPane.YES_NO_OPTION);
                
                if (opcionSeleccionada == JOptionPane.YES_OPTION) {
                    try {
                        String username = (String) table.getModel().getValueAt(filaSeleccionada, 0);

                        if (username == null || username.trim().isEmpty()) {
                            throw new CampoVacioException("El nombre de usuario seleccionado es inválido o nulo.");
                        }
                        
                        api.activarUsuario(username);
                        JOptionPane.showMessageDialog(null, "Usuario activado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        actualizarTabla();

                    } catch (CampoVacioException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Selección", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        desactivarButton = new JButton("Desactivar");
        
        desactivarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = table.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int reply = JOptionPane.showConfirmDialog(null,
                        "¿Está seguro que desea cambiar el estado del Usuario?", "Confirmar cambio de estado.",
                        JOptionPane.YES_NO_OPTION);
                
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        String username = (String) table.getModel().getValueAt(filaSeleccionada, 0);

                        if (username == null || username.trim().isEmpty()) {
                            throw new CampoVacioException("El nombre de usuario seleccionado es inválido o nulo.");
                        }

                        api.desactivarUsuario(username);
                        JOptionPane.showMessageDialog(null, "Usuario desactivado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        actualizarTabla();

                    } catch (CampoVacioException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Selección", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        JButton cerrarButton = new JButton("Cerrar");
        cerrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        JPanel pnlBotonesOperaciones = new JPanel();
        pnlBotonesOperaciones.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        contentPane.add(pnlBotonesOperaciones, BorderLayout.SOUTH);
        pnlBotonesOperaciones.add(desactivarButton);
        pnlBotonesOperaciones.add(activarButton);
        pnlBotonesOperaciones.add(cerrarButton);

        // --- 2. CONFIGURAR ESTADO INICIAL ---
        habilitarBotones(false);
        
        // --- 3. CARGAR DATOS AL FINAL (Cuando todo ya existe) ---
        actualizarTabla(); 
    }

    // Metodos
    private void habilitarBotones(boolean b) {
        // Se agregan chequeos de nulidad por seguridad extra, aunque moviendo actualizarTabla al final ya no debería fallar
        if (activarButton != null) {
            activarButton.setEnabled(b);
        }
        if (desactivarButton != null) {
            desactivarButton.setEnabled(b);
        }
    }

    private void actualizarTabla() {
        DefaultTableModel modelo = (DefaultTableModel) table.getModel();
        modelo.setRowCount(0); 

        try {
            List<UsuarioDTO> usuarios = api.obtenerUsuarios();

            if (usuarios == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar la lista de usuarios.");
            }

            for (UsuarioDTO u : usuarios) {
                modelo.addRow(new Object[] { u.getUsername(), u.getNombre(), u.getEmail(), u.getEstado(), u.getRol() });
            }
        } catch (ObjetoNuloException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Carga de Datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            habilitarBotones(false);
        }
    }
}