package ar.edu.unrn.seminario.gui;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ar.edu.unrn.seminario.dto.BienDTO;

public class BienTableModel extends AbstractTableModel {

    //define el formato de fecha para mostrar
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private List<BienDTO> bienes;
    private final String[] columnNames = {"Categoria", "Cantidad", "Estado", "Fecha de Vencimiento", "Descripcion"};

    public BienTableModel(List<BienDTO> bienes) {
        this.bienes = bienes;
    }

    //metodo para actualizar la lista de datos y refrescar la JTable
    public void setBienes(List<BienDTO> nuevosBienes) {
        this.bienes = nuevosBienes;
        fireTableDataChanged(); //notifica a la JTable que los datos han cambiado
    }

    @Override
    public int getRowCount() {
        return bienes != null ? bienes.size() : 0; //maneja si la lista es nula
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (bienes == null || rowIndex < 0 || rowIndex >= bienes.size()) {
            return null;
        }

        BienDTO bien = bienes.get(rowIndex);
        if (bien == null) {
            return "Nulo"; //maneja si el objeto BienDTO es nulo en la lista
        }
        
        //uso de constantes para los tipos de estado, mejor que usar el literal 0.
        //asumiendo BienDTO.TIPO_NUEVO = 0 y BienDTO.TIPO_USADO = 1 (o cualquier otro valor)
        final String ESTADO_NUEVO = "Nuevo";
        final String ESTADO_USADO = "Usado";
        final int TIPO_NUEVO_CONSTANTE = 0; // Reemplazar con BienDTO.TIPO_NUEVO si está definido

        switch (columnIndex) {
            case 0: //categoria
                return mapCategoriaAString(bien.getCategoria());
            case 1: //cCantidad
                return bien.getCantidad();
            case 2: //estado
                //usando constante o valor de BienDTO si es posible.
                return bien.getTipo() == TIPO_NUEVO_CONSTANTE ? ESTADO_NUEVO : ESTADO_USADO; 
            case 3: // Fecha de Vencimiento
                //mejora: Formatea la fecha a dd/MM/yyyy
                return bien.getFechaVencimiento() != null 
                       ? bien.getFechaVencimiento().format(DATE_FORMATTER) 
                       : "N/A";
            case 4: //descripcion
                return bien.getDescripcion();
            default:
                return null;
        }
    }

    private String mapCategoriaAString(int categoriaId) {
        switch (categoriaId) {
            case BienDTO.CATEGORIA_ROPA:
                return "Ropa";
            case BienDTO.CATEGORIA_MUEBLES:
                return "Muebles";
            case BienDTO.CATEGORIA_ALIMENTOS:
                return "Alimentos";
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS:
                return "Electrodomesticos";
            case BienDTO.CATEGORIA_HERRAMIENTAS:
                return "Herramientas";
            case BienDTO.CATEGORIA_JUGUETES:
                return "Juguetes";
            case BienDTO.CATEGORIA_LIBROS:
                return "Libros";
            case BienDTO.CATEGORIA_MEDICAMENTOS:
                return "Medicamentos";
            case BienDTO.CATEGORIA_HIGIENE:
                return "Higiene";
            default:
                return "Otros";
        }
    }
}