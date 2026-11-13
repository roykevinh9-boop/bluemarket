package com.bluemarket.service;

import com.bluemarket.model.Inventario;
import com.bluemarket.model.Producto;
import java.util.ArrayList;
import java.util.List;

public class InventarioService {
    private final Inventario inventario;
    private final List<Runnable> observadores = new ArrayList<>();

    public InventarioService(Inventario inventario) {
        this.inventario = java.util.Objects.requireNonNull(inventario);
    }

    public Inventario getInventario() { return inventario; }
    public List<Producto> listarProductos() { return inventario.listarProductos(); }
    public Producto buscarProducto(long id) { return inventario.buscarProducto(id); }

    public void agregarProducto(Producto producto) {
        inventario.agregarProducto(producto);
        notificarCambios();
    }

    public void actualizarStock(long id, int cantidad) {
        inventario.actualizarStock(id, cantidad);
        notificarCambios();
    }

    public long siguienteId() {
        return listarProductos().stream().mapToLong(Producto::getId).max().orElse(0) + 1;
    }

    public void agregarObservador(Runnable observador) { observadores.add(observador); }
    public void quitarObservador(Runnable observador) { observadores.remove(observador); }

    public void notificarCambios() {
        List.copyOf(observadores).forEach(Runnable::run);
    }
}
