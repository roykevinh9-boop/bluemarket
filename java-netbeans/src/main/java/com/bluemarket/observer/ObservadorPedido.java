package com.bluemarket.observer;

import com.bluemarket.model.EstadoPedido;
import com.bluemarket.model.Pedido;


@FunctionalInterface
public interface ObservadorPedido {
    void alCambiarEstado(Pedido pedido, EstadoPedido estadoAnterior, EstadoPedido estadoNuevo);
}
