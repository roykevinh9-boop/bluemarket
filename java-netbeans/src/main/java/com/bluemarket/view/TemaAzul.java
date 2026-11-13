package com.bluemarket.view;

import com.bluemarket.model.Pedido;
import com.bluemarket.model.DetallePedido;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class TemaAzul {
    public static final Color AZUL = new Color(23, 91, 181);
    public static final Color OSCURO = new Color(17, 43, 83);
    public static final Color FONDO = new Color(240, 246, 255);
    public static final Color SUAVE = new Color(217, 233, 255);
    public static final Color TEXTO = new Color(28, 47, 73);

    private TemaAzul() { }

    public static void configurar() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo iniciar la interfaz.", ex);
        }
        Font fuente = new Font("SansSerif", Font.PLAIN, 13);
        for (String clave : new String[]{"Label.font", "Button.font", "Table.font", "TextField.font",
                "ComboBox.font", "TabbedPane.font", "Spinner.font", "TableHeader.font"}) {
            UIManager.put(clave, fuente);
        }
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("TabbedPane.selected", SUAVE);
        UIManager.put("Table.selectionBackground", SUAVE);
        UIManager.put("Table.selectionForeground", OSCURO);
        UIManager.put("TextField.selectionBackground", SUAVE);
    }

    public static JPanel contenido(JFrame ventana, String titulo, int ancho, int alto) {
        ventana.setTitle("BlueMarket | " + titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(ancho, alto);
        ventana.setMinimumSize(new Dimension(860, 580));
        ventana.setLocationRelativeTo(null);
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        ventana.setContentPane(panel);
        return panel;
    }

    public static JPanel cabecera(String titulo, String subtitulo) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(OSCURO);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel marca = new JLabel(titulo);
        marca.setFont(new Font("SansSerif", Font.BOLD, 25));
        marca.setForeground(Color.WHITE);
        JLabel descripcion = new JLabel(subtitulo);
        descripcion.setForeground(new Color(197, 219, 255));
        panel.add(marca, BorderLayout.NORTH);
        panel.add(descripcion, BorderLayout.SOUTH);
        return panel;
    }

    public static JButton boton(String texto, String nombre) {
        JButton boton = new JButton(texto);
        boton.setName(nombre);
        boton.setBackground(AZUL);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL), new EmptyBorder(9, 13, 9, 13)));
        return boton;
    }

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
            @Override
            public Class<?> getColumnClass(int columna) {
                return getRowCount() == 0 ? Object.class : getValueAt(0, columna).getClass();
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo, String nombre) {
        JTable tabla = new JTable(modelo);
        tabla.setName(nombre);
        tabla.setRowHeight(31);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(SUAVE);
        tabla.getTableHeader().setBackground(AZUL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
        return tabla;
    }

    public static JLabel aviso() {
        JLabel aviso = new JLabel(" ");
        aviso.setName("aviso");
        aviso.setBorder(new EmptyBorder(6, 4, 6, 4));
        aviso.setForeground(AZUL);
        return aviso;
    }

    public static void mensaje(JLabel aviso, String texto, boolean error) {
        aviso.setForeground(error ? new Color(163, 35, 45) : AZUL);
        aviso.setText(texto);
        aviso.setToolTipText(texto);
    }

    public static String soles(BigDecimal monto) {
        return "S/ " + monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static BigDecimal importe(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe un monto válido, por ejemplo 25.90.");
        }
    }

    public static String detalle(Pedido pedido) {
        if (pedido == null) { return "Selecciona un pedido para ver sus productos."; }
        StringBuilder texto = new StringBuilder("Pedido #" + pedido.getId() + " · " + pedido.getEstado());
        texto.append("\nEntrega: ").append(pedido.getDireccionEntrega())
                .append("\nRepartidor: ").append(pedido.getRepartidor() == null ? "Sin asignar" : pedido.getRepartidor().getNombres())
                .append("\nPago: ").append(pedido.getPago()).append(" · ")
                .append(pedido.getPago().revisarEstadoPago() ? "Aprobado" : "Pendiente").append("\n\n");
        for (DetallePedido item : pedido.getDetalles()) {
            texto.append(item.getCantidad()).append(" × ").append(item.getProducto().getNombre())
                    .append(" — ").append(soles(item.getSubtotal())).append("\n");
        }
        texto.append("\nSubtotal: ").append(soles(pedido.getSubtotal()))
                .append(" | Delivery: ").append(soles(pedido.getCostoDelivery()))
                .append(" | Total: ").append(soles(pedido.getTotal()));
        return texto.toString();
    }

    public static void enEdt(Runnable tarea) {
        if (SwingUtilities.isEventDispatchThread()) { tarea.run(); }
        else { SwingUtilities.invokeLater(tarea); }
    }
}
