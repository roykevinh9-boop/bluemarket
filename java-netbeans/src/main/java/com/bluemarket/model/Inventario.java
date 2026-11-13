package com.bluemarket.model;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    public Inventario(List<Producto> productosIniciales) {
        if (productosIniciales == null) {
            throw new IllegalArgumentException("El catálogo es obligatorio.");
        }
        productosIniciales.forEach(this::agregarProducto);
    }

    public List<Producto> listarProductos() {
        return List.copyOf(productos);
    }

    public Producto buscarProducto(long id) {
        return productos.stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto #" + id + "."));
    }

    public void agregarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (productos.stream().anyMatch(p -> p.getId() == producto.getId())) {
            throw new IllegalArgumentException("El ID del producto ya existe.");
        }
        productos.add(producto);
    }

    public void actualizarStock(long id, int cantidad) {
        buscarProducto(id).actualizarStock(cantidad);
    }
}
