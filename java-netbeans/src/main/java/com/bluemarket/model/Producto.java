package com.bluemarket.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Producto {
    private long id;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private int stock;

    public Producto(long id, String nombre, BigDecimal precio, int stock) {
        this(id, nombre, "General", precio, stock);
    }

    public Producto(long id, String nombre, String categoria, BigDecimal precio, int stock) {
        setId(id);
        setNombre(nombre);
        setCategoria(categoria);
        setPrecio(precio);
        setStock(stock);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser positivo.");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del producto es inválido.");
        }
        this.nombre = nombre.trim();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().length() < 2) {
            throw new IllegalArgumentException("La categoría del producto es inválida.");
        }
        this.categoria = categoria.trim();
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        BigDecimal redondeado = precio.setScale(2, RoundingMode.HALF_UP);
        if (redondeado.signum() <= 0) {
            throw new IllegalArgumentException("El precio mínimo es S/ 0.01.");
        }
        this.precio = redondeado;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock;
    }

    public void disminuirStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > stock) {
            throw new IllegalArgumentException("Stock insuficiente para " + nombre + ".");
        }
        stock -= cantidad;
    }

    public void actualizarStock(int cantidad) {
        setStock(cantidad);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
