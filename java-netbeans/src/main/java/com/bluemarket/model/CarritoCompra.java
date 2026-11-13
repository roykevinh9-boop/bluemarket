package com.bluemarket.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CarritoCompra {
    private final Map<Long, Integer> productos = new LinkedHashMap<>();
    private final Inventario inventario;

    public CarritoCompra(Inventario inventario) {
        this.inventario = java.util.Objects.requireNonNull(inventario);
    }

    public Map<Long, Integer> getProductos() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(productos));
    }

    public void agregarProducto(long id, int cantidad) {
        Producto producto = inventario.buscarProducto(id);
        int actual = productos.getOrDefault(id, 0);
        if (cantidad <= 0 || cantidad > producto.getStock() - actual) {
            throw new IllegalArgumentException("Cantidad inválida o stock insuficiente.");
        }
        productos.put(id, actual + cantidad);
    }

    public void quitarProducto(long id) {
        productos.remove(id);
    }

    public BigDecimal calcularSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> item : productos.entrySet()) {
            total = total.add(inventario.buscarProducto(item.getKey()).getPrecio()
                    .multiply(BigDecimal.valueOf(item.getValue())));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void vaciarCarrito() {
        productos.clear();
    }
}
