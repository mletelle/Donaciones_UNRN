package ar.edu.unrn.seminario.modelo;

public class Administrador implements IRol {

    @Override
    public boolean isDonante() {
        return false;
    }

    @Override
    public boolean isAdministrador() {
        return true;
    }

    @Override
    public boolean isVoluntario() {
        return false;
    }
}