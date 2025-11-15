package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.VoluntarioDTO;
import ar.edu.unrn.seminario.api.IApi;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.ObjetoNuloException; 

public class ListadoVisitasDialog extends JDialog {

    public ListadoVisitasDialog(IApi api, VoluntarioDTO voluntario) {
        
        try {
            // --- 1. USO DE OBJETONULOEXCEPTION (Validar Voluntario y API) ---
            if (voluntario == null) {
                throw new ObjetoNuloException("Error: Se requiere un objeto Voluntario válido para mostrar el historial.");
            }
            // ---------------------------------------------------------------
            
            setTitle("Historial de Visitas de " + voluntario.getNombre() + " " + voluntario.getApellido());
            setSize(900, 400);
            setModal(true);
            setLocationRelativeTo(null);
    
            String[] columnNames = {"Fecha", "Donante", "Resultado", "Bienes Retirados", "Observaciones"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
    
            List<VisitaDTO> visitas = api.obtenerVisitasPorVoluntario(voluntario); 
            
            // --- 2. USO DE OBJETONULOEXCEPTION (Validar resultado de API) ---
            if (visitas == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar el historial de visitas.");
            }
            // ---------------------------------------------------------------

            if (visitas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay visitas registradas para este voluntario.", "Información", JOptionPane.INFORMATION_MESSAGE);
                // Si está vacío, dispose() debe ejecutarse después de mostrar el mensaje.
                SwingUtilities.invokeLater(() -> dispose()); 
                return;
            }
            
            for (VisitaDTO visita : visitas) {
                // --- 3. USO DE OBJETONULOEXCEPTION (Validar datos internos de la visita) ---
                if (visita.getFechaDeVisita() == null) {
                    // Se lanza la excepción para forzar el manejo centralizado de errores.
                    throw new ObjetoNuloException("Una visita no pudo ser cargada debido a que la fecha es nula. ID de la visita: [Pendiente de implementar ID]");
                }
                // -------------------------------------------------------------------------

                // Formatear la lista de bienes como texto
                String bienesTexto;
                if (visita.getBienesRetirados() != null && !visita.getBienesRetirados().isEmpty()) {
                    bienesTexto = String.join(", ", visita.getBienesRetirados());
                } else {
                    bienesTexto = "-";
                }
                
                tableModel.addRow(new Object[]{
                    visita.getFechaDeVisita(),
                    visita.getDonante() != null ? visita.getDonante() : "Sin datos",
                    visita.getResultado() != null ? visita.getResultado() : "Sin resultado",
                    bienesTexto,
                    visita.getObservacion() != null ? visita.getObservacion() : ""
                });
            }
    
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
    
            add(scrollPane, BorderLayout.CENTER);
    
            JButton closeButton = new JButton("Cerrar");
            closeButton.addActionListener(e -> dispose());
    
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
    
            add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (ObjetoNuloException ex) {
            // Manejo de la excepción custom para datos nulos o parámetros nulos
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Carga de Historial", JOptionPane.ERROR_MESSAGE);
            // Si hay un error, cierra el diálogo inmediatamente.
            SwingUtilities.invokeLater(() -> dispose());
        } catch (Exception ex) {
            // Manejo de otros errores no esperados (ej. error de conexión de la API)
            JOptionPane.showMessageDialog(null, "Ocurrió un error inesperado al cargar las visitas: " + ex.getMessage(), "Error General", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> dispose());
        }
    }
}