package ar.edu.unrn.seminario.gui;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import ar.edu.unrn.seminario.dto.BienDTO;

public class BienTableModel extends AbstractTableModel {

    private final List<BienDTO> bienes;
    private final String[] columnNames = {"Categoría", "Cantidad", "Estado", "Fecha de Vencimiento", "Descripción"};

    public BienTableModel(List<BienDTO> bienes) {
        this.bienes = bienes;
    }

    @Override
    public int getRowCount() {
        return bienes.size();
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
        BienDTO bien = bienes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return mapCategoriaToString(bien.getCategoria());
            case 1:
                return bien.getCantidad();
            case 2:
                return bien.getTipo() == 0 ? "Nuevo" : "Usado";
            case 3:
                return bien.getFechaVencimiento() != null ? bien.getFechaVencimiento().toString() : "N/A";
            case 4:
                return bien.getDescripcion();
            default:
                return null;
        }
    }

    private String mapCategoriaToString(int categoriaId) {
        switch (categoriaId) {
            case BienDTO.CATEGORIA_ROPA:
                return "Ropa";
            case BienDTO.CATEGORIA_MUEBLES:
                return "Muebles";
            case BienDTO.CATEGORIA_ALIMENTOS:
                return "Alimentos";
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS:
                return "Electrodomésticos";
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