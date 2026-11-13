package com.bluemarket.model;

import com.bluemarket.observer.ObservadorPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    public static final BigDecimal COSTO_DELIVERY = new BigDecimal("5.90");
    private final long id;
    private final Cliente cliente;
    private final String direccionEntrega;
    private final LocalDateTime fechaRegistro = LocalDateTime.now();
    private final List<DetallePedido> detalles;
    private final IPago pago;
    private final List<ObservadorPedido> observadores = new ArrayList<>();
    private Repartidor repartidor;
    private EstadoPedido estado = EstadoPedido.REGISTRADO;

    public Pedido(long id, Cliente cliente, List<DetallePedido> detalles, IPago pago) {
        if (id <= 0 || cliente == null || detalles == null || detalles.isEmpty() || pago == null) {
            throw new IllegalArgumentException("Los datos del pedido están incompletos.");
        }
        this.id = id;
        this.cliente = cliente;
        this.direccionEntrega = cliente.getDireccion();
        // El pedido conserva las cantidades y los precios al momento de la compra.
        this.detalles = detalles.stream().map(d -> {
            DetallePedido copia = new DetallePedido(d.getProducto(), d.getCantidad());
            copia.setPrecioUnitario(d.getPrecioUnitario());
            return copia;
        }).toList();
        this.pago = pago;
    }

    public long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public EstadoPedido getEstado() { return estado; }
    public Repartidor getRepartidor() { return repartidor; }
    public IPago getPago() { return pago; }

    public List<DetallePedido> getDetalles() {
        return detalles.stream().map(d -> {
            DetallePedido copia = new DetallePedido(d.getProducto(), d.getCantidad());
            copia.setPrecioUnitario(d.getPrecioUnitario());
            return copia;
        }).toList();
    }

    public BigDecimal getSubtotal() {
        return detalles.stream().map(DetallePedido::getSubtotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    public BigDecimal getCostoDelivery() { return COSTO_DELIVERY; }
    public BigDecimal getTotal() { return getSubtotal().add(COSTO_DELIVERY); }

    public void asignarRepartidor(Repartidor repartidor) {
        if (estado == EstadoPedido.EN_CAMINO || estado == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("El pedido ya salió a reparto.");
        }
        this.repartidor = java.util.Objects.requireNonNull(repartidor, "Selecciona un repartidor.");
    }

    public void setEstado(EstadoPedido nuevo) {
        if (nuevo == estado) { return; }
        // El flujo del delivery avanza un estado a la vez.
        if (nuevo == null || estado == EstadoPedido.ENTREGADO || nuevo.ordinal() != estado.ordinal() + 1) {
            throw new IllegalStateException("Debes respetar el orden de los estados del pedido.");
        }
        if (nuevo == EstadoPedido.EN_CAMINO && repartidor == null) {
            throw new IllegalStateException("Asigna un repartidor antes de iniciar el reparto.");
        }
        EstadoPedido anterior = estado;
        estado = nuevo;
        for (ObservadorPedido observador : List.copyOf(observadores)) {
            observador.alCambiarEstado(this, anterior, nuevo);
        }
    }

    public void agregarObservador(ObservadorPedido observador) {
        if (observador != null && !observadores.contains(observador)) { observadores.add(observador); }
    }

    public void quitarObservador(ObservadorPedido observador) { observadores.remove(observador); }
}
