package com.bluemarket.model;

import java.math.BigDecimal;

public interface IPago {
    boolean procesarPago(BigDecimal monto);
    boolean revisarEstadoPago();
}
