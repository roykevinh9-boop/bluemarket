package com.bluemarket.model;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class DetallePedido {
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetallePedido(Producto producto, int cantidad) {
        setProducto(producto);
        setCantidad(cantidad);
        setPrecioUnitario(producto.getPrecio());
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero.");
        }
        BigDecimal redondeado = precioUnitario.setScale(2, RoundingMode.HALF_UP);
        if (redondeado.signum() <= 0) {
            throw new IllegalArgumentException("El precio mínimo es S/ 0.01.");
        }
        this.precioUnitario = redondeado;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
