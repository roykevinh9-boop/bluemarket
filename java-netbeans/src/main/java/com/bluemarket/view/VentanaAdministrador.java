package com.bluemarket.view;

import com.bluemarket.model.*;
import com.bluemarket.service.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaAdministrador extends JFrame {
    private final InventarioService inventario;
    private final PedidoService pedidos;
    private final Administrador administrador;
    private final List<Proveedor> proveedores;
    private final DefaultTableModel modelo = TemaAzul.modelo("ID", "Producto", "Categoría", "Precio S/", "Stock");
    private final JTable tabla = TemaAzul.tabla(modelo, "inventario");
    private final JTextField nombre = new JTextField();
    private final JTextField categoria = new JTextField("General");
    private final JTextField precio = new JTextField();
    private final JSpinner stockNuevo = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1));
    private final JSpinner stockActual = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 1));
    private final JComboBox<Proveedor> proveedor;
    private final JComboBox<Repartidor> repartidor;
    private final JButton preparar = TemaAzul.boton("Preparar pedido", "preparar");
    private final JButton asignar = TemaAzul.boton("Asignar repartidor", "asignar");
    private final JButton actualizarStock = TemaAzul.boton("Guardar stock", "guardarStock");
    private final JLabel aviso = TemaAzul.aviso();
    private final JTextArea textoReporte = new JTextArea();
    private final PanelPedidos panelPedidos;
    private final Runnable cambioInventario = () -> TemaAzul.enEdt(this::mostrarProductos);
    private Reporte reporte;

    public VentanaAdministrador(InventarioService inventario, PedidoService pedidos, Administrador administrador,
            List<Repartidor> repartidores, List<Proveedor> proveedores) {
        this.inventario = inventario;
        this.pedidos = pedidos;
        this.administrador = administrador;
        this.proveedores = List.copyOf(proveedores);
        repartidor = new JComboBox<>(repartidores.toArray(new Repartidor[0]));
        proveedor = new JComboBox<>(proveedores.toArray(new Proveedor[0]));
        panelPedidos = new PanelPedidos(pedidos, () -> administrador.consultarPedidos(pedidos));
        nombre.setName("nombreProducto");
        categoria.setName("categoriaProducto");
        precio.setName("precioProducto");
        stockNuevo.setName("stockNuevo");
        stockActual.setName("stockActual");
        JPanel contenido = TemaAzul.contenido(this, "Administrador", 1120, 740);
        contenido.add(TemaAzul.cabecera("BlueMarket · Administración", administrador.getNombres() + " · "
                + administrador.getCodigoEmpleado() + " · Inventario, pedidos y reportes"), BorderLayout.NORTH);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Inventario", crearInventario());
        pestanas.addTab("Gestión de pedidos", crearPedidos());
        pestanas.addTab("Proveedores", crearProveedores());
        pestanas.addTab("Reportes", crearReportes());
        contenido.add(pestanas, BorderLayout.CENTER);
        contenido.add(aviso, BorderLayout.SOUTH);
        preparar.addActionListener(e -> prepararPedido());
        asignar.addActionListener(e -> asignarRepartidor());
        panelPedidos.alSeleccionar(this::actualizarAcciones);
        inventario.agregarObservador(cambioInventario);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                inventario.quitarObservador(cambioInventario);
                panelPedidos.cerrar();
            }
        });
        mostrarProductos();
    }

    private JPanel crearInventario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(370);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            Producto seleccionado = productoSeleccionado();
            actualizarStock.setEnabled(seleccionado != null);
            if (seleccionado != null) { stockActual.setValue(seleccionado.getStock()); }
        });
        JPanel pie = new JPanel(new BorderLayout(10, 10));
        JPanel edicion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        edicion.add(new JLabel("Stock del producto seleccionado:"));
        edicion.add(stockActual);
        edicion.add(actualizarStock);
        actualizarStock.addActionListener(e -> actualizarStock());
        pie.add(edicion, BorderLayout.NORTH);
        JPanel alta = new JPanel(new BorderLayout(8, 8));
        alta.setBorder(BorderFactory.createTitledBorder("Agregar producto"));
        JPanel campos = new JPanel(new GridLayout(2, 5, 8, 5));
        for (String etiqueta : new String[]{"Nombre", "Categoría", "Precio S/", "Stock inicial", "Proveedor"}) {
            campos.add(new JLabel(etiqueta));
        }
        campos.add(nombre); campos.add(categoria); campos.add(precio); campos.add(stockNuevo); campos.add(proveedor);
        alta.add(campos, BorderLayout.CENTER);
        JButton agregar = TemaAzul.boton("Agregar producto", "crearProducto");
        agregar.addActionListener(e -> agregarProducto());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(agregar);
        alta.add(botones, BorderLayout.SOUTH);
        pie.add(alta, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPedidos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(panelPedidos, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        acciones.add(preparar);
        acciones.add(new JLabel("Repartidor:"));
        acciones.add(repartidor);
        acciones.add(asignar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearProveedores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        DefaultTableModel modeloProveedor = TemaAzul.modelo("ID", "Proveedor", "RUC", "Productos");
        JTable tablaProveedor = TemaAzul.tabla(modeloProveedor, "proveedores");
        JTextArea productos = new JTextArea(8, 40);
        productos.setEditable(false);
        Runnable cargar = () -> {
            modeloProveedor.setRowCount(0);
            for (Proveedor item : proveedores) {
                modeloProveedor.addRow(new Object[]{item.getIdProveedor(), item.getNombre(), item.getRuc(), item.consultarProductos().size()});
            }
            productos.setText("Selecciona un proveedor para ver su catálogo.");
        };
        tablaProveedor.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProveedor.getSelectedRow();
            if (fila >= 0) {
                long id = ((Number) modeloProveedor.getValueAt(tablaProveedor.convertRowIndexToModel(fila), 0)).longValue();
                Proveedor seleccionado = proveedores.stream().filter(p -> p.getIdProveedor() == id).findFirst().orElseThrow();
                StringBuilder texto = new StringBuilder(seleccionado.getNombre() + "\n\n");
                seleccionado.consultarProductos().forEach(p -> texto.append(p.getNombre()).append("\n"));
                productos.setText(texto.toString());
                productos.setCaretPosition(0);
            }
        });
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaProveedor), new JScrollPane(productos));
        split.setResizeWeight(0.4);
        split.setDividerLocation(160);
        panel.add(split, BorderLayout.CENTER);
        JButton refrescar = TemaAzul.boton("Actualizar catálogo", "refrescarProveedores");
        refrescar.addActionListener(e -> cargar.run());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.add(refrescar);
        panel.add(acciones, BorderLayout.SOUTH);
        cargar.run();
        return panel;
    }

    private JPanel crearReportes() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        textoReporte.setEditable(false);
        textoReporte.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textoReporte.setText("Genera un reporte para consultar el resumen de los pedidos.");
        textoReporte.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.add(new JScrollPane(textoReporte), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        JButton generar = TemaAzul.boton("Generar reporte", "generarReporte");
        JButton guardar = TemaAzul.boton("Guardar TXT", "guardarReporte");
        JButton imprimir = TemaAzul.boton("Imprimir", "imprimirReporte");
        generar.addActionListener(e -> generarReporte());
        guardar.addActionListener(e -> guardarReporte());
        imprimir.addActionListener(e -> imprimirReporte());
        acciones.add(generar); acciones.add(guardar); acciones.add(imprimir);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    public void mostrarProductos() {
        Producto seleccionado = productoSeleccionado();
        modelo.setRowCount(0);
        for (Producto producto : administrador.gestionarProductos(inventario)) {
            modelo.addRow(new Object[]{producto.getId(), producto.getNombre(), producto.getCategoria(), producto.getPrecio(), producto.getStock()});
        }
        if (seleccionado != null) {
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                if (((Number) modelo.getValueAt(fila, 0)).longValue() == seleccionado.getId()) {
                    int visible = tabla.convertRowIndexToView(fila);
                    tabla.setRowSelectionInterval(visible, visible);
                }
            }
        }
        actualizarStock.setEnabled(tabla.getSelectedRow() >= 0);
    }

    private Producto productoSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : inventario.buscarProducto(
                ((Number) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0)).longValue());
    }

    public void agregarProducto() {
        try {
            stockNuevo.commitEdit();
            Producto producto = new Producto(inventario.siguienteId(), nombre.getText(), categoria.getText(),
                    TemaAzul.importe(precio.getText()), ((Number) stockNuevo.getValue()).intValue());
            inventario.agregarProducto(producto);
            Proveedor elegido = (Proveedor) proveedor.getSelectedItem();
            if (elegido != null) { elegido.asociarProducto(producto); }
            nombre.setText(""); precio.setText(""); stockNuevo.setValue(0);
            informar("Producto #" + producto.getId() + " agregado al inventario.", false);
        } catch (Exception ex) { informar(ex.getMessage(), true); }
    }

    public void actualizarStock() {
        try {
            stockActual.commitEdit();
            Producto producto = productoSeleccionado();
            if (producto == null) { throw new IllegalArgumentException("Selecciona un producto."); }
            administrador.gestionarInventario(inventario, producto.getId(), ((Number) stockActual.getValue()).intValue());
            informar("Stock actualizado para " + producto.getNombre() + ".", false);
        } catch (Exception ex) { informar(ex.getMessage(), true); }
    }

    public void prepararPedido() {
        try {
            pedidos.prepararPedido(panelPedidos.seleccionado());
            informar("Pedido en preparación.", false);
        } catch (RuntimeException ex) { informar(ex.getMessage(), true); }
    }

    public void asignarRepartidor() {
        try {
            administrador.asignarRepartidor(pedidos, panelPedidos.seleccionado(), (Repartidor) repartidor.getSelectedItem());
            informar("Repartidor asignado al pedido.", false);
        } catch (RuntimeException ex) { informar(ex.getMessage(), true); }
    }

    private void actualizarAcciones() {
        Pedido pedido = panelPedidos.seleccionado();
        preparar.setEnabled(pedido != null && pedido.getEstado() == EstadoPedido.REGISTRADO);
        asignar.setEnabled(pedido != null && (pedido.getEstado() == EstadoPedido.REGISTRADO || pedido.getEstado() == EstadoPedido.EN_PREPARACION));
    }

    public void generarReporte() {
        reporte = administrador.generarReporte(pedidos);
        textoReporte.setText(reporte.generarReporte());
        textoReporte.setCaretPosition(0);
        informar("Reporte generado con los pedidos actuales.", false);
    }

    private void guardarReporte() {
        if (reporte == null) { generarReporte(); }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new java.io.File("BlueMarket-reporte-" + reporte.getIdReporte() + ".txt"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) { return; }
        if (selector.getSelectedFile().exists()) {
            int respuesta = JOptionPane.showConfirmDialog(this, "El archivo ya existe. ¿Deseas reemplazarlo?",
                    "Guardar reporte", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) { return; }
        }
        try {
            Files.writeString(selector.getSelectedFile().toPath(), reporte.generarReporte(), StandardCharsets.UTF_8);
            informar("Reporte guardado.", false);
        } catch (java.io.IOException ex) { informar("No se pudo guardar el reporte: " + ex.getMessage(), true); }
    }

    private void imprimirReporte() {
        if (reporte == null) { generarReporte(); }
        try {
            informar(reporte.imprimirReporte() ? "Reporte enviado a impresión." : "Impresión cancelada.", false);
        } catch (java.awt.print.PrinterException ex) { informar("No se pudo imprimir: " + ex.getMessage(), true); }
    }

    private void informar(String texto, boolean error) { TemaAzul.mensaje(aviso, texto, error); }
}
