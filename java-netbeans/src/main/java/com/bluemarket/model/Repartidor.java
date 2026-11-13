package com.bluemarket.model;

import com.bluemarket.service.PedidoService;
import java.util.List;

public class Repartidor extends Usuario {
    private final String placaVehiculo;

    public Repartidor(String dni, String nombres, String telefono, String placaVehiculo) {
        super(dni, nombres, telefono);
        if (placaVehiculo == null || placaVehiculo.trim().length() < 5) {
            throw new IllegalArgumentException("La placa debe tener al menos 5 caracteres.");
        }
        this.placaVehiculo = placaVehiculo.trim().toUpperCase();
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public List<Pedido> consultarPedidos(PedidoService servicio) {
        return servicio.listarPedidosAsignados(this);
    }

    public void aceptarPedido(PedidoService servicio, Pedido pedido) {
        servicio.marcarEnCamino(pedido, this);
    }

    public void confirmarEntrega(PedidoService servicio, Pedido pedido) {
        servicio.confirmarEntrega(pedido, this);
    }
}
