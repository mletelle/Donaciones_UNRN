package ar.edu.unrn.seminario.dto;

public class OrdenEntregaDTO {
    private int id;
    private String fecha;
    private String estado;
    private String descripcionBienes;
    private String beneficiario;
    private String voluntario;

    public OrdenEntregaDTO(int id, String fecha, String estado, String descripcionBienes) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.descripcionBienes = descripcionBienes;
        this.beneficiario = "";
        this.voluntario = "";
    }

    public OrdenEntregaDTO(int id, String fecha, String estado, String beneficiario, String voluntario) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.descripcionBienes = "";
        this.beneficiario = beneficiario;
        this.voluntario = voluntario;
    }

    public int getId() {
        return id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getDescripcionBienes() {
        return descripcionBienes;
    }

    public String getBeneficiario() {
        return beneficiario;
    }

    public String getVoluntario() {
        return voluntario;
    }
    
    @Override
    public String toString() {
        return "Orden #" + id + " - " + fecha + " (" + estado + ")";
    }
}