package com.bluemarket.view;

import com.bluemarket.model.*;
import com.bluemarket.service.PedidoService;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VentanaRepartidor extends JFrame {
    private final PedidoService pedidos;
    private final Repartidor repartidor;
    private final PanelPedidos panelPedidos;
    private final JButton camino = TemaAzul.boton("Marcar en camino", "camino");
    private final JButton entregar = TemaAzul.boton("Confirmar entrega", "entregar");
    private final JLabel aviso = TemaAzul.aviso();

    public VentanaRepartidor(PedidoService pedidos, Repartidor repartidor) {
        this.pedidos = pedidos;
        this.repartidor = repartidor;
        panelPedidos = new PanelPedidos(pedidos, () -> repartidor.consultarPedidos(pedidos));
        JPanel contenido = TemaAzul.contenido(this, "Repartidor", 1060, 710);
        contenido.add(TemaAzul.cabecera("BlueMarket · Repartidor", repartidor.getNombres()
                + " · Vehículo " + repartidor.getPlacaVehiculo() + " · Tus pedidos asignados"), BorderLayout.NORTH);
        contenido.add(panelPedidos, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(8, 8));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        acciones.add(camino); acciones.add(entregar);
        pie.add(acciones, BorderLayout.NORTH);
        pie.add(aviso, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);
        camino.addActionListener(e -> marcarEnCamino());
        entregar.addActionListener(e -> confirmarEntrega());
        panelPedidos.alSeleccionar(this::actualizarAcciones);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) { panelPedidos.cerrar(); }
        });
    }

    public void mostrarPedidosAsignados() { panelPedidos.actualizar(); }

    private void actualizarAcciones() {
        Pedido pedido = panelPedidos.seleccionado();
        camino.setEnabled(pedido != null && pedido.getEstado() == EstadoPedido.EN_PREPARACION);
        entregar.setEnabled(pedido != null && pedido.getEstado() == EstadoPedido.EN_CAMINO);
    }

    public void marcarEnCamino() {
        try {
            repartidor.aceptarPedido(pedidos, panelPedidos.seleccionado());
            TemaAzul.mensaje(aviso, "Pedido en camino a su destino.", false);
        } catch (RuntimeException ex) { TemaAzul.mensaje(aviso, ex.getMessage(), true); }
    }

    public void confirmarEntrega() {
        try {
            repartidor.confirmarEntrega(pedidos, panelPedidos.seleccionado());
            TemaAzul.mensaje(aviso, "Entrega confirmada. El cliente ya puede ver el cambio.", false);
        } catch (RuntimeException ex) { TemaAzul.mensaje(aviso, ex.getMessage(), true); }
    }
}
