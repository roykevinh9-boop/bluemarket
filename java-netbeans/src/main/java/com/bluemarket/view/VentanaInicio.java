package com.bluemarket.view;

import com.bluemarket.model.*;
import com.bluemarket.service.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class VentanaInicio extends JFrame {
    private final InventarioService inventario;
    private final PedidoService pedidos;
    private final Cliente cliente;
    private final Administrador administrador;
    private final List<Repartidor> repartidores;
    private final List<Proveedor> proveedores;
    private VentanaCliente ventanaCliente;
    private VentanaAdministrador ventanaAdministrador;
    private VentanaRepartidor ventanaRepartidor;
    private final ButtonGroup perfiles = new ButtonGroup();
    private final JButton entrar = TemaAzul.boton("Entrar como Cliente", "iniciarSesion");
    private String rolSeleccionado = "Cliente";

    public VentanaInicio(InventarioService inventario, PedidoService pedidos, Cliente cliente,
            Administrador administrador, List<Repartidor> repartidores, List<Proveedor> proveedores) {
        this.inventario = inventario;
        this.pedidos = pedidos;
        this.cliente = cliente;
        this.administrador = administrador;
        this.repartidores = List.copyOf(repartidores);
        this.proveedores = List.copyOf(proveedores);
        JPanel contenido = TemaAzul.contenido(this, "Iniciar sesión", 960, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        contenido.add(TemaAzul.cabecera("BlueMarket", "Tus compras, desde el catálogo hasta la puerta de tu casa."), BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(12, 20));
        JLabel titulo = new JLabel("Iniciar sesión · ¿Quién eres?");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        centro.add(titulo, BorderLayout.NORTH);
        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 18, 0));
        tarjetas.add(tarjeta("Cliente", "Compra y sigue tus pedidos", "Catálogo · Carrito · Pagos", "elegirCliente"));
        tarjetas.add(tarjeta("Administrador", "Organiza la operación", "Inventario · Pedidos · Reportes", "elegirAdministrador"));
        tarjetas.add(tarjeta("Repartidor", "Lleva cada pedido a destino", "Pedidos asignados · Entregas", "elegirRepartidor"));
        centro.add(tarjetas, BorderLayout.CENTER);
        JPanel acceso = new JPanel(new BorderLayout(12, 8));
        JLabel ayuda = new JLabel("Selecciona tu perfil y pulsa Entrar.");
        ayuda.setForeground(TemaAzul.AZUL);
        acceso.add(ayuda, BorderLayout.CENTER);
        entrar.addActionListener(e -> iniciarSesion());
        acceso.add(entrar, BorderLayout.EAST);
        centro.add(acceso, BorderLayout.SOUTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(new JLabel("Inicio por selección de perfil · Cliente, Administrador o Repartidor."), BorderLayout.SOUTH);
    }

    private JPanel tarjeta(String rol, String descripcion, String funciones, String nombre) {
        JPanel panel = new JPanel(new BorderLayout(12, 18));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TemaAzul.SUAVE),
                new EmptyBorder(24, 18, 24, 18)));
        JLabel titulo = new JLabel(rol);
        titulo.setForeground(TemaAzul.AZUL);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);
        JLabel texto = new JLabel("<html>" + descripcion + "<br><br><span style='color:#526781'>" + funciones + "</span></html>");
        panel.add(texto, BorderLayout.CENTER);
        JRadioButton opcion = new JRadioButton("Soy " + rol.toLowerCase(), rol.equals("Cliente"));
        opcion.setName(nombre);
        opcion.setActionCommand(rol);
        opcion.setBackground(Color.WHITE);
        opcion.setForeground(TemaAzul.AZUL);
        opcion.setFont(new Font("SansSerif", Font.BOLD, 14));
        perfiles.add(opcion);
        opcion.addActionListener(e -> {
            rolSeleccionado = rol;
            entrar.setText("Entrar como " + rol);
        });
        panel.add(opcion, BorderLayout.SOUTH);
        return panel;
    }

    public void iniciarSesion() {
        switch (rolSeleccionado) {
            case "Cliente" -> abrirCliente();
            case "Administrador" -> abrirAdministrador();
            case "Repartidor" -> abrirRepartidor();
            default -> throw new IllegalStateException("Selecciona un perfil.");
        }
    }

    public void abrirCliente() {
        if (ventanaCliente == null || !ventanaCliente.isDisplayable()) {
            ventanaCliente = new VentanaCliente(inventario, pedidos, cliente);
        }
        ventanaCliente.setVisible(true);
        ventanaCliente.toFront();
    }

    public void abrirAdministrador() {
        if (ventanaAdministrador == null || !ventanaAdministrador.isDisplayable()) {
            ventanaAdministrador = new VentanaAdministrador(inventario, pedidos, administrador, repartidores, proveedores);
        }
        ventanaAdministrador.setVisible(true);
        ventanaAdministrador.toFront();
    }

    public void abrirRepartidor() {
        if (ventanaRepartidor == null || !ventanaRepartidor.isDisplayable()) {
            ventanaRepartidor = new VentanaRepartidor(pedidos, repartidores.get(0));
        }
        ventanaRepartidor.setVisible(true);
        ventanaRepartidor.toFront();
    }
}
