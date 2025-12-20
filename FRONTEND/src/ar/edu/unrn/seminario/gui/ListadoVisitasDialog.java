package ar.edu.unrn.seminario.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import ar.edu.unrn.seminario.dto.VisitaDTO;
import ar.edu.unrn.seminario.dto.UsuarioDTO;
import ar.edu.unrn.seminario.api.IApi;
// Importaciones de excepciones propias
import ar.edu.unrn.seminario.exception.ObjetoNuloException; 

public class ListadoVisitasDialog extends JDialog {

    public ListadoVisitasDialog(IApi api, UsuarioDTO voluntario) {
        
        try {
            if (voluntario == null) {
                throw new ObjetoNuloException("Error: Se requiere un objeto Voluntario válido para mostrar el historial.");
            }
            
            setTitle("Historial de Visitas de " + voluntario.getNombre() + " " + voluntario.getApellido());
            setSize(900, 400);
            setModal(true);
            setLocationRelativeTo(null);
    
            String[] columnNames = {"Fecha", "Donante", "Resultado", "Observaciones"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
    
            List<VisitaDTO> visitas = api.obtenerVisitasPorVoluntario(voluntario); 
            
            if (visitas == null) {
                throw new ObjetoNuloException("La API devolvió un resultado nulo. No se pudo cargar el historial de visitas.");
            }

            if (visitas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay visitas registradas para este voluntario.", "Información", JOptionPane.INFORMATION_MESSAGE);
                SwingUtilities.invokeLater(() -> dispose()); 
                return;
            }
            
            for (VisitaDTO visita : visitas) {
                if (visita.getFechaDeVisita() == null) {
                    throw new ObjetoNuloException("Una visita no pudo ser cargada debido a que la fecha es nula. ID de la visita: [Pendiente de implementar ID]");
                }
                
                tableModel.addRow(new Object[]{
                    visita.getFechaDeVisita(),
                    visita.getDonante() != null ? visita.getDonante() : "Sin datos",
                    visita.getResultado() != null ? visita.getResultado() : "Sin resultado",
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
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error de Carga de Historial", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> dispose());
        }
    }
}