package com.bluemarket.model;

import java.math.BigDecimal;

public class PagoEfectivo implements IPago {
    private final BigDecimal montoRecibido;
    private boolean aprobado;

    public PagoEfectivo(BigDecimal montoRecibido) {
        if (montoRecibido == null || montoRecibido.signum() < 0) {
            throw new IllegalArgumentException("El monto recibido no puede ser negativo.");
        }
        this.montoRecibido = montoRecibido;
    }

    public BigDecimal getMontoRecibido() { return montoRecibido; }

    @Override
    public boolean procesarPago(BigDecimal monto) {
        aprobado = monto != null && monto.signum() > 0 && montoRecibido.compareTo(monto) >= 0;
        return aprobado;
    }

    @Override
    public boolean revisarEstadoPago() { return aprobado; }

    @Override
    public String toString() { return "Efectivo"; }
}
