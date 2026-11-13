package com.bluemarket.view;

import com.bluemarket.model.*;
import com.bluemarket.observer.*;
import com.bluemarket.service.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class VentanaCliente extends JFrame implements ObservadorPedido, ObservadorListaPedidos {
    private final InventarioService inventario;
    private final PedidoService pedidos;
    private final Cliente cliente;
    private final CarritoCompra carrito;
    private final DefaultTableModel modeloCatalogo = TemaAzul.modelo("ID", "Producto", "Categoría", "Precio S/", "Stock");
    private final DefaultTableModel modeloCarrito = TemaAzul.modelo("ID", "Producto", "Cantidad", "Precio S/", "Subtotal S/");
    private final JTable tablaCatalogo = TemaAzul.tabla(modeloCatalogo, "catalogo");
    private final JTable tablaCarrito = TemaAzul.tabla(modeloCarrito, "carrito");
    private final JTextField filtro = new JTextField(24);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JTextField direccion = new JTextField(27);
    private final JComboBox<String> formaPago = new JComboBox<>(new String[]{"Tarjeta", "Efectivo"});
    private final JTextField datoPago = new JTextField("DEMO-001", 12);
    private final JLabel etiquetaPago = new JLabel("Referencia de tarjeta:");
    private final JLabel totales = new JLabel();
    private final JLabel aviso = TemaAzul.aviso();
    private final JButton agregar = TemaAzul.boton("Agregar al carrito", "agregar");
    private final JButton quitar = TemaAzul.boton("Quitar producto", "quitar");
    private final JButton vaciar = TemaAzul.boton("Vaciar carrito", "vaciar");
    private final JButton confirmar = TemaAzul.boton("Confirmar pedido", "confirmar");
    private final JTabbedPane pestanas = new JTabbedPane();
    private final PanelPedidos seguimiento;
    private final List<Pedido> observados = new ArrayList<>();
    private final Runnable actualizarInventario = () -> TemaAzul.enEdt(this::mostrarCatalogo);

    public VentanaCliente(InventarioService inventario, PedidoService pedidos, Cliente cliente) {
        this.inventario = inventario;
        this.pedidos = pedidos;
        this.cliente = cliente;
        carrito = cliente.obtenerCarrito(inventario.getInventario());
        direccion.setText(cliente.getDireccion());
        direccion.setName("direccion");
        cantidad.setName("cantidad");
        formaPago.setName("formaPago");
        datoPago.setName("datoPago");
        seguimiento = new PanelPedidos(pedidos, () -> pedidos.listarPedidosCliente(cliente));
        JPanel contenido = TemaAzul.contenido(this, "Cliente", 1100, 730);
        contenido.add(TemaAzul.cabecera("BlueMarket · Cliente", "Hola, " + cliente.getNombres() + ". Elige tus productos y sigue tu entrega."), BorderLayout.NORTH);
        pestanas.addTab("Catálogo", crearCatalogo());
        pestanas.addTab("Carrito y pago", crearCarrito());
        pestanas.addTab("Mis pedidos", seguimiento);
        contenido.add(pestanas, BorderLayout.CENTER);
        contenido.add(aviso, BorderLayout.SOUTH);
        agregar.addActionListener(e -> agregarACarrito());
        quitar.addActionListener(e -> quitarDelCarrito());
        vaciar.addActionListener(e -> { carrito.vaciarCarrito(); mostrarCarrito(); informar("Carrito vaciado.", false); });
        confirmar.addActionListener(e -> confirmarCarrito());
        formaPago.addActionListener(e -> actualizarFormaPago());
        tablaCatalogo.getSelectionModel().addListSelectionListener(e -> actualizarCantidad());
        tablaCarrito.getSelectionModel().addListSelectionListener(e -> quitar.setEnabled(tablaCarrito.getSelectedRow() >= 0));
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarCatalogo(); }
            public void removeUpdate(DocumentEvent e) { filtrarCatalogo(); }
            public void changedUpdate(DocumentEvent e) { filtrarCatalogo(); }
        });
        inventario.agregarObservador(actualizarInventario);
        pedidos.agregarObservadorListaPedidos(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                inventario.quitarObservador(actualizarInventario);
                pedidos.quitarObservadorListaPedidos(VentanaCliente.this);
                observados.forEach(p -> p.quitarObservador(VentanaCliente.this));
                seguimiento.cerrar();
            }
        });
        mostrarCatalogo();
        mostrarCarrito();
        observarPedidos();
    }

    private JPanel crearCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscar.add(new JLabel("Buscar producto o categoría:"));
        buscar.add(filtro);
        panel.add(buscar, BorderLayout.NORTH);
        tablaCatalogo.getColumnModel().getColumn(1).setPreferredWidth(360);
        tablaCatalogo.getColumnModel().getColumn(0).setMaxWidth(60);
        panel.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        acciones.add(new JLabel("Cantidad:"));
        acciones.add(cantidad);
        acciones.add(agregar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearCarrito() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(360);
        tablaCarrito.getColumnModel().getColumn(0).setMaxWidth(60);
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(8, 8));
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; formulario.add(new JLabel("Dirección de entrega:"), c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL; formulario.add(direccion, c);
        c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
        c.gridx = 0; c.gridy = 1; formulario.add(new JLabel("Forma de pago:"), c);
        c.gridx = 1; formulario.add(formaPago, c);
        c.gridx = 2; formulario.add(etiquetaPago, c);
        c.gridx = 3; formulario.add(datoPago, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 4;
        JLabel demo = new JLabel("Pago de demostración · Delivery fijo: S/ 5.90");
        demo.setForeground(TemaAzul.AZUL);
        formulario.add(demo, c);
        totales.setFont(new Font("SansSerif", Font.BOLD, 18));
        totales.setForeground(TemaAzul.OSCURO);
        JPanel resumen = new JPanel(new BorderLayout());
        resumen.add(totales, BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(quitar); botones.add(vaciar); botones.add(confirmar);
        resumen.add(botones, BorderLayout.EAST);
        pie.add(formulario, BorderLayout.CENTER);
        pie.add(resumen, BorderLayout.SOUTH);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    public void mostrarCatalogo() {
        Long seleccionado = idSeleccionado(tablaCatalogo);
        modeloCatalogo.setRowCount(0);
        for (Producto producto : inventario.listarProductos()) {
            modeloCatalogo.addRow(new Object[]{producto.getId(), producto.getNombre(), producto.getCategoria(),
                producto.getPrecio(), producto.getStock()});
        }
        if (seleccionado != null) {
            for (int fila = 0; fila < modeloCatalogo.getRowCount(); fila++) {
                if (((Number) modeloCatalogo.getValueAt(fila, 0)).longValue() == seleccionado) {
                    int visible = tablaCatalogo.convertRowIndexToView(fila);
                    if (visible >= 0) { tablaCatalogo.setRowSelectionInterval(visible, visible); }
                }
            }
        }
        if (tablaCatalogo.getSelectedRow() < 0 && tablaCatalogo.getRowCount() > 0) {
            tablaCatalogo.setRowSelectionInterval(0, 0);
        }
        actualizarCantidad();
    }

    private void filtrarCatalogo() {
        TableRowSorter<?> sorter = (TableRowSorter<?>) tablaCatalogo.getRowSorter();
        sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(filtro.getText().trim()), 1, 2));
        if (tablaCatalogo.getSelectedRow() < 0 && tablaCatalogo.getRowCount() > 0) {
            tablaCatalogo.setRowSelectionInterval(0, 0);
        }
        actualizarCantidad();
    }

    private Long idSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : ((Number) tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), 0)).longValue();
    }

    private void actualizarCantidad() {
        Long id = idSeleccionado(tablaCatalogo);
        int disponible = id == null ? 0 : Math.max(0,
                inventario.buscarProducto(id).getStock() - carrito.getProductos().getOrDefault(id, 0));
        int valor = disponible == 0 ? 0 : Math.min(Math.max(1, ((Number) cantidad.getValue()).intValue()), disponible);
        cantidad.setModel(new SpinnerNumberModel(valor, disponible == 0 ? 0 : 1, disponible, 1));
        cantidad.setEnabled(disponible > 0);
        agregar.setEnabled(disponible > 0);
    }

    public void agregarACarrito() {
        try {
            cantidad.commitEdit();
            Long id = idSeleccionado(tablaCatalogo);
            if (id == null) { throw new IllegalArgumentException("Selecciona un producto."); }
            carrito.agregarProducto(id, ((Number) cantidad.getValue()).intValue());
            mostrarCarrito();
            actualizarCantidad();
            informar("Producto agregado. Revisa la pestaña Carrito y pago.", false);
        } catch (Exception ex) { informar(ex.getMessage() == null ? "Revisa la cantidad." : ex.getMessage(), true); }
    }

    private void quitarDelCarrito() {
        Long id = idSeleccionado(tablaCarrito);
        if (id != null) {
            carrito.quitarProducto(id);
            mostrarCarrito();
            actualizarCantidad();
            informar("Producto retirado del carrito.", false);
        }
    }

    private void mostrarCarrito() {
        modeloCarrito.setRowCount(0);
        for (Map.Entry<Long, Integer> item : carrito.getProductos().entrySet()) {
            Producto producto = inventario.buscarProducto(item.getKey());
            modeloCarrito.addRow(new Object[]{producto.getId(), producto.getNombre(), item.getValue(),
                producto.getPrecio(), producto.getPrecio().multiply(BigDecimal.valueOf(item.getValue()))});
        }
        boolean vacio = carrito.getProductos().isEmpty();
        BigDecimal total = carrito.calcularSubtotal().add(vacio ? BigDecimal.ZERO : Pedido.COSTO_DELIVERY);
        totales.setText("Total: " + TemaAzul.soles(total));
        totales.setToolTipText("Subtotal: " + TemaAzul.soles(carrito.calcularSubtotal()) + " + delivery");
        confirmar.setEnabled(!vacio);
        vaciar.setEnabled(!vacio);
        quitar.setEnabled(false);
        pestanas.setTitleAt(1, "Carrito y pago (" + carrito.getProductos().values().stream().mapToInt(Integer::intValue).sum() + ")");
        actualizarCantidad();
    }

    private void actualizarFormaPago() {
        boolean efectivo = formaPago.getSelectedIndex() == 1;
        etiquetaPago.setText(efectivo ? "Monto recibido S/:" : "Referencia de tarjeta:");
        datoPago.setText(efectivo ? carrito.calcularSubtotal().add(Pedido.COSTO_DELIVERY).toPlainString() : "DEMO-001");
    }

    public void confirmarCarrito() {
        try {
            cliente.setDireccion(direccion.getText());
            IPago pago = formaPago.getSelectedIndex() == 0 ? new PagoTarjeta(datoPago.getText())
                    : new PagoEfectivo(TemaAzul.importe(datoPago.getText()));
            Pedido pedido = pedidos.crearPedido(cliente, carrito, pago);
            mostrarCarrito();
            seguimiento.seleccionar(pedido.getId());
            pestanas.setSelectedIndex(2);
            informar("Pedido #" + pedido.getId() + " registrado · Total " + TemaAzul.soles(pedido.getTotal()), false);
        } catch (RuntimeException ex) { informar(ex.getMessage(), true); }
    }

    private void observarPedidos() {
        for (Pedido pedido : pedidos.listarPedidosCliente(cliente)) {
            if (!observados.contains(pedido)) {
                pedido.agregarObservador(this);
                observados.add(pedido);
            }
        }
    }

    private void informar(String texto, boolean error) { TemaAzul.mensaje(aviso, texto, error); }

    @Override
    public void alActualizarListaPedidos() { TemaAzul.enEdt(this::observarPedidos); }

    @Override
    public void alCambiarEstado(Pedido pedido, EstadoPedido anterior, EstadoPedido nuevo) {
        TemaAzul.enEdt(() -> informar("Pedido #" + pedido.getId() + " · " + nuevo, false));
    }
}
