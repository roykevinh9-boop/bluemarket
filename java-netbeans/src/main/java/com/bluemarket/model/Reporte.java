package com.bluemarket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JTextArea;
import java.awt.print.PrinterException;

public class Reporte {
    private static final AtomicLong SECUENCIA = new AtomicLong();
    private final long idReporte = SECUENCIA.incrementAndGet();
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();
    private final String tipo;
    private final String contenido;

    public Reporte(String tipo, List<Pedido> pedidos) {
        this.tipo = tipo;
        BigDecimal total = pedidos.stream().map(Pedido::getTotal)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        StringBuilder texto = new StringBuilder("BLUE MARKET — " + tipo + "\n");
        texto.append("Reporte #").append(idReporte).append(" | ")
                .append(fechaGeneracion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\n\nPedidos registrados: ").append(pedidos.size())
                .append("\nTotal de pedidos: S/ ").append(total.toPlainString()).append("\n");
        for (EstadoPedido estado : EstadoPedido.values()) {
            texto.append(estado).append(": ")
                    .append(pedidos.stream().filter(p -> p.getEstado() == estado).count()).append("\n");
        }
        texto.append("\nDETALLE\n");
        for (Pedido pedido : pedidos) {
            texto.append('#').append(pedido.getId()).append(" | ")
                    .append(pedido.getCliente().getNombres()).append(" | ")
                    .append(pedido.getEstado()).append(" | S/ ")
                    .append(pedido.getTotal().toPlainString()).append("\n");
        }
        contenido = texto.toString();
    }

    public long getIdReporte() { return idReporte; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public String getTipo() { return tipo; }
    public String generarReporte() { return contenido; }

    public boolean imprimirReporte() throws PrinterException {
        return new JTextArea(contenido).print();
    }
}
