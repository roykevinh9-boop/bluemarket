package com.bluemarket;

import com.bluemarket.model.Producto;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogoDemo {
    private CatalogoDemo() { }

    public static List<Producto> crearProductos() {
        return List.of(
                new Producto(1, "Arroz Superior Tottus Bolsa 5 kg", "Abarrotes", new BigDecimal("16.00"), 28),
                new Producto(2, "Arroz Faraón Extra Bolsa 5 kg", "Abarrotes", new BigDecimal("22.90"), 20),
                new Producto(3, "Arroz Costeño Extra Bolsa 5 kg", "Abarrotes", new BigDecimal("22.90"), 24),
                new Producto(4, "Aceite Vegetal Primor Clásico 900 ml", "Abarrotes", new BigDecimal("9.20"), 25),
                new Producto(5, "Azúcar Rubia 1 kg", "Abarrotes", new BigDecimal("4.80"), 30),
                new Producto(6, "Lenteja Bebé Tottus 500 g", "Abarrotes", new BigDecimal("3.30"), 22),
                new Producto(7, "Fideos Spaghetti Molitalia 950 g", "Abarrotes", new BigDecimal("3.90"), 30),
                new Producto(8, "Spaghetti Don Vittorio 950 g", "Abarrotes", new BigDecimal("4.50"), 26),
                new Producto(9, "Fideo Linguine Molitalia 950 g", "Abarrotes", new BigDecimal("3.90"), 20),
                new Producto(10, "Pasta Rigatoni Don Vittorio 500 g", "Abarrotes", new BigDecimal("3.20"), 18),
                new Producto(11, "Fideos Tottus Tornillo 250 g", "Abarrotes", new BigDecimal("1.70"), 35),
                new Producto(12, "Filete de Atún Tottus en Agua 140 g", "Conservas", new BigDecimal("4.50"), 32),
                new Producto(13, "Atún Tottus en Aceite Sixpack 140 g", "Conservas", new BigDecimal("21.30"), 14),
                new Producto(14, "Leche Gloria Reconstituida Entera 390 g", "Lácteos", new BigDecimal("4.20"), 40),
                new Producto(15, "Leche Laive Sin Lactosa 390 g", "Lácteos", new BigDecimal("4.40"), 28),
                new Producto(16, "Leche UHT Entera Gloria Pack 3 x 946 ml", "Lácteos", new BigDecimal("16.50"), 16),
                new Producto(17, "Leche Gloria Zero Lacto Pack 3 x 800 ml", "Lácteos", new BigDecimal("13.40"), 18),
                new Producto(18, "Yogurt Gloria Fresa 1.6 kg", "Lácteos", new BigDecimal("8.90"), 20),
                new Producto(19, "Yogurt Gloria Vainilla 1.6 kg", "Lácteos", new BigDecimal("8.90"), 20),
                new Producto(20, "Yogurt Griego Vakimu Original 960 g", "Lácteos", new BigDecimal("12.60"), 14),
                new Producto(21, "Mantequilla Gloria con Sal 180 g", "Lácteos", new BigDecimal("10.30"), 16),
                new Producto(22, "Queso Edam Braedt en Láminas 180 g", "Lácteos", new BigDecimal("10.10"), 15),
                new Producto(23, "Queso Parmesano Rallado Gloria 35 g", "Lácteos", new BigDecimal("4.50"), 18),
                new Producto(24, "Pan de Molde Blanco", "Panadería", new BigDecimal("8.90"), 18),
                new Producto(25, "Huevos Pardos Bandeja x 15", "Frescos", new BigDecimal("10.50"), 22),
                new Producto(26, "Pollo Fresco Tottus con Menudencia x kg", "Carnes", new BigDecimal("6.50"), 18),
                new Producto(27, "Filete de Pechuga de Pollo x kg", "Carnes", new BigDecimal("13.90"), 16),
                new Producto(28, "Carne Molida de Res x kg", "Carnes", new BigDecimal("24.90"), 12),
                new Producto(29, "Papa Blanca Yungay x kg", "Frutas y verduras", new BigDecimal("1.99"), 35),
                new Producto(30, "Tomate Italiano x kg", "Frutas y verduras", new BigDecimal("6.29"), 30),
                new Producto(31, "Cebolla Roja x kg", "Frutas y verduras", new BigDecimal("3.49"), 32),
                new Producto(32, "Zanahoria x kg", "Frutas y verduras", new BigDecimal("3.00"), 30),
                new Producto(33, "Lechuga Americana Unidad", "Frutas y verduras", new BigDecimal("3.80"), 18),
                new Producto(34, "Mandarina Sin Pepa x kg", "Frutas y verduras", new BigDecimal("2.69"), 28),
                new Producto(35, "Limón Tahití x kg", "Frutas y verduras", new BigDecimal("3.99"), 25),
                new Producto(36, "Plátano de Seda x kg", "Frutas y verduras", new BigDecimal("4.50"), 25),
                new Producto(37, "Manzana Roja x kg", "Frutas y verduras", new BigDecimal("6.90"), 20),
                new Producto(38, "Coca-Cola Pack 2 Botellas 1 L", "Bebidas", new BigDecimal("7.90"), 24),
                new Producto(39, "Coca-Cola Sin Azúcar Sixpack 300 ml", "Bebidas", new BigDecimal("8.50"), 20),
                new Producto(40, "Inca Kola Sin Azúcar Sixpack 300 ml", "Bebidas", new BigDecimal("9.00"), 20),
                new Producto(41, "Agua San Mateo 2.5 L", "Bebidas", new BigDecimal("3.20"), 30),
                new Producto(42, "Jugo Pulp Durazno 1 L", "Bebidas", new BigDecimal("4.50"), 24),
                new Producto(43, "Café Instantáneo Altomayo 180 g", "Desayuno", new BigDecimal("13.90"), 15),
                new Producto(44, "Milo Activ-Go 400 g", "Desayuno", new BigDecimal("15.50"), 18),
                new Producto(45, "Galletas Victoria Integrackers Pack 9", "Snacks", new BigDecimal("7.90"), 25),
                new Producto(46, "Galleta Costa Gran Cereal Sixpack 216 g", "Snacks", new BigDecimal("6.50"), 22),
                new Producto(47, "Papas Pringles Sour Cream 109 g", "Snacks", new BigDecimal("7.90"), 20),
                new Producto(48, "Papas Lays Ondas Picante 130 g", "Snacks", new BigDecimal("7.50"), 20),
                new Producto(49, "Chocolate Sublime Clásico 80 g", "Snacks", new BigDecimal("4.50"), 30),
                new Producto(50, "Avena Quaker 500 g", "Desayuno", new BigDecimal("8.90"), 20),
                new Producto(51, "Polo Textura Mujer Redwood", "Ropa", new BigDecimal("29.90"), 10),
                new Producto(52, "Blusa Macramé Mujer Redwood", "Ropa", new BigDecimal("19.90"), 8),
                new Producto(53, "Body Suplex Mujer Redwood", "Ropa", new BigDecimal("24.90"), 8),
                new Producto(54, "Polo Tejido Hombre Redwood", "Ropa", new BigDecimal("34.90"), 10),
                new Producto(55, "Polo Piqué Neru Hombre Redwood", "Ropa", new BigDecimal("34.90"), 10),
                new Producto(56, "Polo Boxy Hombre Redwood", "Ropa", new BigDecimal("29.90"), 12),
                new Producto(57, "Polo con Cuello 100% Algodón Hombre Redwood", "Ropa", new BigDecimal("19.90"), 12),
                new Producto(58, "Polo Oversize 100% Algodón Hombre Redwood", "Ropa", new BigDecimal("24.90"), 10),
                new Producto(59, "Polo Hombre Spalding", "Ropa", new BigDecimal("19.90"), 9),
                new Producto(60, "Polo Manga Corta Raglan Hombre Spalding", "Ropa", new BigDecimal("19.90"), 9),
                new Producto(61, "Jabón Dove Original Pack 3", "Cuidado personal", new BigDecimal("12.90"), 20),
                new Producto(62, "Jabón Protex Avena Pack 3", "Cuidado personal", new BigDecimal("10.90"), 18),
                new Producto(63, "Máquina de Afeitar Gillette Mach3 Aqua Grip", "Cuidado personal", new BigDecimal("21.90"), 14),
                new Producto(64, "Desodorante en Gel Gillette Cool Wave 45 g", "Cuidado personal", new BigDecimal("14.90"), 16),
                new Producto(65, "Rasuradora Gillette Prestobarba 3 Carbono Pack 2", "Cuidado personal", new BigDecimal("18.90"), 16)
        );
    }
}
