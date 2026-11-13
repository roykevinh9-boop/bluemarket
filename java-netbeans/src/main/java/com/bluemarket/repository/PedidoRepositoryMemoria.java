package com.bluemarket.repository;

import com.bluemarket.model.Pedido;
import com.bluemarket.model.EstadoPedido;
import com.bluemarket.observer.ObservadorPedido;
import com.bluemarket.observer.ObservadorListaPedidos;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoRepositoryMemoria implements ObservadorPedido {
    private final List<Pedido> pedidos = new ArrayList<>();
    private final List<ObservadorListaPedidos> observadores = new ArrayList<>();

    public void guardar(Pedido pedido) {
        if (pedido == null || buscarPorId(pedido.getId()).isPresent()) {
            throw new IllegalArgumentException("El pedido es nulo o su ID ya existe.");
        }
        pedidos.add(pedido);
        pedido.agregarObservador(this);
        notificarLista();
    }

    public Optional<Pedido> buscarPorId(long id) {
        return pedidos.stream().filter(p -> p.getId() == id).findFirst();
    }

    public List<Pedido> listarTodos() { return List.copyOf(pedidos); }

    public void agregarObservadorLista(ObservadorListaPedidos observador) {
        if (observador != null && !observadores.contains(observador)) { observadores.add(observador); }
    }

    public void quitarObservadorLista(ObservadorListaPedidos observador) { observadores.remove(observador); }

    public void notificarLista() {
        for (ObservadorListaPedidos observador : List.copyOf(observadores)) {
            observador.alActualizarListaPedidos();
        }
    }

    @Override
    public void alCambiarEstado(Pedido pedido, EstadoPedido anterior, EstadoPedido nuevo) {
        notificarLista();
    }
}
