package com.bluemarket.model;

import com.bluemarket.service.InventarioService;
import com.bluemarket.service.PedidoService;
import java.util.List;

public class Administrador extends Usuario {
    private final String codigoEmpleado;

    public Administrador(String dni, String nombres, String telefono, String codigoEmpleado) {
        super(dni, nombres, telefono);
        if (codigoEmpleado == null || codigoEmpleado.isBlank()) {
            throw new IllegalArgumentException("El código de empleado es obligatorio.");
        }
        this.codigoEmpleado = codigoEmpleado.trim();
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public List<Producto> gestionarProductos(InventarioService servicio) {
        return servicio.listarProductos();
    }

    public void gestionarInventario(InventarioService servicio, long id, int cantidad) {
        servicio.actualizarStock(id, cantidad);
    }

    public List<Pedido> consultarPedidos(PedidoService servicio) {
        return servicio.listarPedidos();
    }

    public void asignarRepartidor(PedidoService servicio, Pedido pedido, Repartidor repartidor) {
        servicio.asignarRepartidor(pedido, repartidor);
    }

    public Reporte generarReporte(PedidoService servicio) {
        return new Reporte("Resumen de pedidos", servicio.listarPedidos());
    }
}
