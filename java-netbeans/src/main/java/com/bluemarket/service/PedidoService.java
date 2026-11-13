package com.bluemarket.service;

import com.bluemarket.model.*;
import com.bluemarket.observer.ObservadorListaPedidos;
import com.bluemarket.repository.PedidoRepositoryMemoria;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PedidoService {
    private final InventarioService inventarioService;
    private final PedidoRepositoryMemoria repositorio;
    private final AtomicLong secuencia = new AtomicLong(1000);

    public PedidoService(InventarioService inventarioService, PedidoRepositoryMemoria repositorio) {
        this.inventarioService = java.util.Objects.requireNonNull(inventarioService);
        this.repositorio = java.util.Objects.requireNonNull(repositorio);
    }

    public List<Pedido> listarPedidos() { return repositorio.listarTodos(); }

    public Pedido buscarPedido(long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id + "."));
    }

    public List<Pedido> listarPedidosCliente(Cliente cliente) {
        return listarPedidos().stream().filter(p -> p.getCliente().getDni().equals(cliente.getDni())).toList();
    }

    public List<Pedido> listarPedidosAsignados(Repartidor repartidor) {
        return listarPedidos().stream().filter(p -> p.getRepartidor() != null
                && p.getRepartidor().getDni().equals(repartidor.getDni())).toList();
    }

    public synchronized Pedido crearPedido(Cliente cliente, CarritoCompra carrito, IPago pago) {
        if (cliente == null || carrito == null || pago == null || carrito.getProductos().isEmpty()) {
            throw new IllegalArgumentException("Completa los datos y agrega productos al carrito.");
        }
        List<DetallePedido> detalles = new ArrayList<>();
        // Se valida todo el carrito antes de cobrar o descontar unidades.
        for (Map.Entry<Long, Integer> item : carrito.getProductos().entrySet()) {
            Producto producto = inventarioService.buscarProducto(item.getKey());
            if (item.getValue() <= 0 || item.getValue() > producto.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre() + ".");
            }
            detalles.add(new DetallePedido(producto, item.getValue()));
        }
        Pedido pedido = new Pedido(secuencia.incrementAndGet(), cliente, detalles, pago);
        if (!pago.procesarPago(pedido.getTotal())) {
            throw new IllegalArgumentException("El monto en efectivo no alcanza para cubrir el total.");
        }
        for (DetallePedido detalle : detalles) {
            detalle.getProducto().disminuirStock(detalle.getCantidad());
        }
        carrito.vaciarCarrito();
        repositorio.guardar(pedido);
        inventarioService.notificarCambios();
        return pedido;
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null || buscarPedido(pedido.getId()) != pedido) {
            throw new IllegalArgumentException("Selecciona un pedido registrado.");
        }
    }

    public void prepararPedido(Pedido pedido) {
        validarPedido(pedido);
        if (pedido.getEstado() != EstadoPedido.REGISTRADO) {
            throw new IllegalStateException("Solo se pueden preparar pedidos registrados.");
        }
        cambiarEstado(pedido, EstadoPedido.EN_PREPARACION);
    }

    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        validarPedido(pedido);
        pedido.asignarRepartidor(repartidor);
        repositorio.notificarLista();
    }

    private void validarAsignacion(Pedido pedido, Repartidor repartidor) {
        validarPedido(pedido);
        if (repartidor == null || pedido.getRepartidor() == null
                || !pedido.getRepartidor().getDni().equals(repartidor.getDni())) {
            throw new IllegalStateException("Este pedido no está asignado a este repartidor.");
        }
    }

    public void marcarEnCamino(Pedido pedido, Repartidor repartidor) {
        validarAsignacion(pedido, repartidor);
        if (pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new IllegalStateException("El administrador debe preparar el pedido primero.");
        }
        cambiarEstado(pedido, EstadoPedido.EN_CAMINO);
    }

    public void confirmarEntrega(Pedido pedido, Repartidor repartidor) {
        validarAsignacion(pedido, repartidor);
        if (pedido.getEstado() != EstadoPedido.EN_CAMINO) {
            throw new IllegalStateException("El pedido debe estar en camino para confirmar la entrega.");
        }
        cambiarEstado(pedido, EstadoPedido.ENTREGADO);
    }

    public void cambiarEstado(Pedido pedido, EstadoPedido nuevo) {
        validarPedido(pedido);
        pedido.setEstado(nuevo);
    }

    public void agregarObservadorListaPedidos(ObservadorListaPedidos observador) {
        repositorio.agregarObservadorLista(observador);
    }

    public void quitarObservadorListaPedidos(ObservadorListaPedidos observador) {
        repositorio.quitarObservadorLista(observador);
    }
}
