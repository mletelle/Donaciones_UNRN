package ar.edu.unrn.seminario.servicios;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ar.edu.unrn.seminario.dto.BienDTO;
import ar.edu.unrn.seminario.modelo.Bien;
import ar.edu.unrn.seminario.modelo.CategoriaBien;

public class BienMapper {

    public static BienDTO entidadADTOVisual(Bien bien) {
        String categoriaStr = mapCategoriaToString(bien.obtenerCategoria());
        String estadoStr = "Usado";
        
        String vencimientoStr = "-";
        LocalDate fechaLocalDate = null;

        if (bien.getFecVec() != null) {
            fechaLocalDate = bien.getFecVec().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            vencimientoStr = fechaLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        
        BienDTO dto = new BienDTO();
        dto.setId(bien.getId());
        dto.setDescripcion(bien.getDescripcion());
        dto.setCantidad(bien.obtenerCantidad());
        dto.setCategoria(categoriaEnumToDTO(bien.obtenerCategoria()));
        dto.setCategoriaTexto(categoriaStr);
        dto.setEstadoTexto(estadoStr);
        dto.setFechaVencimiento(fechaLocalDate);
        dto.setVencimientoTexto(vencimientoStr);
        
        return dto;
    }

    public static CategoriaBien DTOCategoriaToEnum(int categoria) {
        switch (categoria) {
            case BienDTO.CATEGORIA_ROPA: return CategoriaBien.ROPA;
            case BienDTO.CATEGORIA_MUEBLES: return CategoriaBien.MUEBLES;
            case BienDTO.CATEGORIA_ALIMENTOS: return CategoriaBien.ALIMENTOS;
            case BienDTO.CATEGORIA_ELECTRODOMESTICOS: return CategoriaBien.ELECTRODOMESTICOS;
            case BienDTO.CATEGORIA_HERRAMIENTAS: return CategoriaBien.HERRAMIENTAS;
            case BienDTO.CATEGORIA_JUGUETES: return CategoriaBien.JUGUETES;
            case BienDTO.CATEGORIA_LIBROS: return CategoriaBien.LIBROS;
            case BienDTO.CATEGORIA_MEDICAMENTOS: return CategoriaBien.MEDICAMENTOS;
            case BienDTO.CATEGORIA_HIGIENE: return CategoriaBien.HIGIENE;
            default: return CategoriaBien.OTROS;
        }
    }
    
    public static int categoriaEnumToDTO(CategoriaBien categoria) {
        switch (categoria) {
            case ROPA: return BienDTO.CATEGORIA_ROPA;
            case MUEBLES: return BienDTO.CATEGORIA_MUEBLES;
            case ALIMENTOS: return BienDTO.CATEGORIA_ALIMENTOS;
            case ELECTRODOMESTICOS: return BienDTO.CATEGORIA_ELECTRODOMESTICOS;
            case HERRAMIENTAS: return BienDTO.CATEGORIA_HERRAMIENTAS;
            case JUGUETES: return BienDTO.CATEGORIA_JUGUETES;
            case LIBROS: return BienDTO.CATEGORIA_LIBROS;
            case MEDICAMENTOS: return BienDTO.CATEGORIA_MEDICAMENTOS;
            case HIGIENE: return BienDTO.CATEGORIA_HIGIENE;
            default: return BienDTO.CATEGORIA_OTROS;
        }
    }

    public static Bien toEntity(BienDTO dto) throws Exception {
        if (dto == null) throw new IllegalArgumentException("dto no puede ser nulo");
        
        CategoriaBien categoria = DTOCategoriaToEnum(dto.getCategoria());
        
        Bien bien = new Bien(dto.getCantidad(), categoria);
        
        if (dto.getDescripcion() != null && !dto.getDescripcion().trim().isEmpty()) {
            bien.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getFechaVencimiento() != null) {
            java.util.Date fechaDB = java.util.Date.from(
                dto.getFechaVencimiento().atStartOfDay(ZoneId.systemDefault()).toInstant()
            );
            bien.setFecVec(fechaDB);
        }
        
        return bien;
    }

    private static String mapCategoriaToString(CategoriaBien categoria) {
        switch (categoria) {
            case ROPA: return "Ropa";
            case MUEBLES: return "Muebles";
            case ALIMENTOS: return "Alimentos";
            case ELECTRODOMESTICOS: return "Electrodomesticos";
            case HERRAMIENTAS: return "Herramientas";
            case JUGUETES: return "Juguetes";
            case LIBROS: return "Libros";
            case MEDICAMENTOS: return "Medicamentos";
            case HIGIENE: return "Higiene";
            default: return "Otros";
        }
    }
}
