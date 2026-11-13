package com.bluemarket;

import com.bluemarket.model.*;
import com.bluemarket.repository.PedidoRepositoryMemoria;
import com.bluemarket.service.*;
import com.bluemarket.view.*;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemaAzul.configurar();
            InventarioService inventario = new InventarioService(new Inventario(CatalogoDemo.crearProductos()));
            PedidoService pedidos = new PedidoService(inventario, new PedidoRepositoryMemoria());
            Cliente cliente = new Cliente("71234567", "Roy Hurtado", "912345678", "Jr. Los Álamos 284, San Miguel, Lima");
            Administrador administrador = new Administrador("70123456", "Ana Torres", "923456789", "ADM-001");
            Repartidor repartidor = new Repartidor("76543210", "Carlos Mendoza", "987654321", "ABC-123");
            Proveedor proveedor = new Proveedor(1, "Distribuidora BlueMarket", "20123456789");
            inventario.listarProductos().forEach(proveedor::asociarProducto);
            new VentanaInicio(inventario, pedidos, cliente, administrador, List.of(repartidor), List.of(proveedor)).setVisible(true);
        });
    }
}
