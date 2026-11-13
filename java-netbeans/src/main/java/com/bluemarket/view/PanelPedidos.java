package com.bluemarket.view;

import com.bluemarket.model.Pedido;
import com.bluemarket.observer.ObservadorListaPedidos;
import com.bluemarket.service.PedidoService;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PanelPedidos extends JPanel implements ObservadorListaPedidos {
    private final PedidoService servicio;
    private final Supplier<List<Pedido>> consulta;
    private final DefaultTableModel modelo = TemaAzul.modelo("ID", "Cliente", "Fecha", "Total", "Estado", "Repartidor");
    private final JTable tabla = TemaAzul.tabla(modelo, "pedidos");
    private final JTextArea detalle = new JTextArea(7, 30);
    private Runnable alSeleccionar = () -> { };
    private boolean actualizando;

    public PanelPedidos(PedidoService servicio, Supplier<List<Pedido>> consulta) {
        super(new BorderLayout(8, 8));
        this.servicio = servicio;
        this.consulta = consulta;
        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detalle.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detalle.setName("detallePedido");
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabla), new JScrollPane(detalle));
        split.setResizeWeight(0.7);
        split.setDividerLocation(250);
        add(split, BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !actualizando) { actualizarDetalle(); }
        });
        servicio.agregarObservadorListaPedidos(this);
        actualizar();
    }

    public Pedido seleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : servicio.buscarPedido(
                ((Number) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0)).longValue());
    }

    public void alSeleccionar(Runnable tarea) {
        alSeleccionar = tarea;
        actualizarDetalle();
    }

    public void seleccionar(long id) {
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            if (((Number) modelo.getValueAt(fila, 0)).longValue() == id) {
                int visible = tabla.convertRowIndexToView(fila);
                tabla.setRowSelectionInterval(visible, visible);
                return;
            }
        }
    }

    public void actualizar() {
        TemaAzul.enEdt(() -> {
            Pedido previo = seleccionado();
            actualizando = true;
            modelo.setRowCount(0);
            for (Pedido pedido : consulta.get()) {
                modelo.addRow(new Object[]{pedido.getId(), pedido.getCliente().getNombres(),
                    pedido.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                    pedido.getTotal(), pedido.getEstado().toString(),
                    pedido.getRepartidor() == null ? "Sin asignar" : pedido.getRepartidor().getNombres()});
            }
            if (previo != null) { seleccionar(previo.getId()); }
            if (tabla.getSelectedRow() < 0 && tabla.getRowCount() > 0) { tabla.setRowSelectionInterval(0, 0); }
            actualizando = false;
            actualizarDetalle();
        });
    }

    private void actualizarDetalle() {
        detalle.setText(TemaAzul.detalle(seleccionado()));
        detalle.setCaretPosition(0);
        alSeleccionar.run();
    }

    @Override
    public void alActualizarListaPedidos() { actualizar(); }

    public void cerrar() { servicio.quitarObservadorListaPedidos(this); }
}
