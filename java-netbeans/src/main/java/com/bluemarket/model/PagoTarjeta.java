package com.bluemarket.model;

import java.math.BigDecimal;

public class PagoTarjeta implements IPago {
    private final String referencia;
    private boolean aprobado;

    public PagoTarjeta(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("Escribe una referencia para el pago con tarjeta.");
        }
        this.referencia = referencia.trim();
    }

    public String getReferencia() { return referencia; }

    @Override
    public boolean procesarPago(BigDecimal monto) {
        // Simulación académica: no se conecta a una pasarela bancaria.
        aprobado = monto != null && monto.signum() > 0;
        return aprobado;
    }

    @Override
    public boolean revisarEstadoPago() { return aprobado; }

    @Override
    public String toString() { return "Tarjeta"; }
}
