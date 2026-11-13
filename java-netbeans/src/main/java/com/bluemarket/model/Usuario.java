package com.bluemarket.model;


public abstract class Usuario {
    private String dni;
    private String nombres;
    private String telefono;

    protected Usuario(String dni, String nombres, String telefono) {
        setDni(dni);
        setNombres(nombres);
        setTelefono(telefono);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos.");
        }
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        if (nombres == null || nombres.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres.");
        }
        this.nombres = nombres.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || !telefono.matches("\\d{9}")) {
            throw new IllegalArgumentException("El teléfono debe contener exactamente 9 dígitos.");
        }
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return nombres + " (DNI " + dni + ")";
    }
}
