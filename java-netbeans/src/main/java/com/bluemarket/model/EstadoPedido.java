package com.bluemarket.model;

public enum EstadoPedido {
    REGISTRADO("Registrado"),
    EN_PREPARACION("En preparación"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado");

    private final String texto;

    EstadoPedido(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
