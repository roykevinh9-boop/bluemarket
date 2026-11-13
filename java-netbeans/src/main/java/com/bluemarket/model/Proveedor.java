package com.bluemarket.model;

import java.util.ArrayList;
import java.util.List;

public class Proveedor {
    private final long idProveedor;
    private final String nombre;
    private final String ruc;
    private final List<Producto> productos = new ArrayList<>();

    public Proveedor(long idProveedor, String nombre, String ruc) {
        if (idProveedor <= 0 || nombre == null || nombre.isBlank()
                || ruc == null || !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("Proveedor inválido: revisa ID, nombre y RUC.");
        }
        this.idProveedor = idProveedor;
        this.nombre = nombre.trim();
        this.ruc = ruc;
    }

    public long getIdProveedor() { return idProveedor; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }

    public void asociarProducto(Producto producto) {
        if (producto != null && !productos.contains(producto)) {
            productos.add(producto);
        }
    }

    public List<Producto> consultarProductos() {
        return List.copyOf(productos);
    }

    @Override
    public String toString() { return nombre; }
}
