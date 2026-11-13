package com.bluemarket.model;

public class Cliente extends Usuario {
    private String direccion;
    private CarritoCompra carrito;

    public CarritoCompra obtenerCarrito(Inventario inventario) {
        if (carrito == null) {
            carrito = new CarritoCompra(inventario);
        }
        return carrito;
    }

    public Cliente(String dni, String nombres, String telefono, String direccion) {
        super(dni, nombres, telefono);
        setDireccion(direccion);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion == null || direccion.trim().length() < 5) {
            throw new IllegalArgumentException("La dirección debe tener al menos 5 caracteres.");
        }
        this.direccion = direccion.trim();
    }
}
