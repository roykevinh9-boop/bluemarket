const categories = [
  { key: 'Frescos', name: 'Frutas y verduras', color: 'green', sample: /palta/i },
  { key: 'Despensa', name: 'Abarrotes', color: 'sand', sample: /arroz.*costeño/i },
  { key: 'Carnes', name: 'Carnes y comidas', color: 'peach', sample: /pollo/i },
  { key: 'Lácteos', name: 'Lácteos y huevos', color: 'blue', sample: /leche.*gloria/i },
  { key: 'Panadería', name: 'Panadería', color: 'sand', sample: /croissant|pan francés/i },
  { key: 'Bebidas', name: 'Bebidas', color: 'peach', sample: /inca kola/i },
  { key: 'Moda', name: 'Moda y calzado', color: 'lavender', sample: /polo/i },
  { key: 'Electrohogar', name: 'Electrohogar', color: 'blue', sample: /freidora/i },
  { key: 'Hogar', name: 'Hogar y limpieza', color: 'mint', sample: /lavavajilla|detergente/i },
  { key: 'Cuidado personal', name: 'Cuidado personal', color: 'blue', sample: /shampoo/i },
  { key: 'Mascotas', name: 'Mascotas', color: 'green', sample: /ricocan|dog chow/i },
];

class Producto {
  constructor(id, nombre, presentacion, categoria, precio, stock, anterior = 0, tallas = [], marca = '', tipo = '') {
    Object.assign(this, { id, nombre, presentacion, categoria, precio, stock, anterior, tallas, marca, tipo });
  }
}

// Los precios se expresan en céntimos para calcular el total sin redondeos.
const productosAnteriores = [
  new Producto('palta', 'Palta Hass', 'Malla de 500 g', 'Frescos', 899, 24, 1190),
  new Producto('arroz', 'Arroz extra Costeño', 'Bolsa de 1 kg', 'Despensa', 570, 35, 690),
  new Producto('leche', 'Leche entera Gloria', 'Caja de 1 L', 'Lácteos', 550, 25),
  new Producto('polo', 'Polo básico de algodón', 'Blanco · Unisex', 'Moda', 2490, 15, 3490, ['S', 'M', 'L', 'XL']),
  new Producto('fresas', 'Fresas frescas', 'Bandeja de 250 g', 'Frescos', 749, 14),
  new Producto('aceite', 'Aceite vegetal Primor', 'Botella de 900 ml', 'Despensa', 899, 22, 1090),
  new Producto('yogur', 'Yogur de fresa Gloria', 'Pote de 500 g', 'Lácteos', 690, 18),
  new Producto('gaseosa', 'Gaseosa cola sin azúcar', 'Botella de 1.5 L', 'Bebidas', 650, 25),
  new Producto('jugo', 'Jugo de naranja', 'Botella de 1 L', 'Bebidas', 750, 18, 920),
  new Producto('jean', 'Jean recto azul', 'Denim · Unisex', 'Moda', 5990, 12, 7990, ['28', '30', '32', '34']),
  new Producto('zapatillas', 'Zapatillas urbanas blancas', 'Par · Unisex', 'Moda', 7490, 10, 9990, ['36', '37', '38', '39', '40', '41', '42']),
  new Producto('bolso', 'Bolsa de lona reutilizable', 'Beige · Talla única', 'Moda', 1490, 16),
  new Producto('toallas', 'Set de toallas de algodón', 'Blanco · 2 unidades', 'Hogar', 2990, 10),
  new Producto('lavavajilla', 'Lavavajilla líquido', 'Limón · 750 ml', 'Hogar', 790, 22, 990),
  new Producto('detergente', 'Detergente en polvo', 'Bolsa de 2 kg', 'Hogar', 1990, 20, 2390),
  new Producto('recipiente', 'Recipiente hermético de vidrio', 'Tapa azul · 1 L', 'Hogar', 1890, 12),
];

const catalogo = datosProductos.map(({ id, nombre, presentacion, categoria, precio, stock, anterior, tallas, marca, tipo }) =>
  new Producto(id, nombre, presentacion, categoria, precio, stock, anterior, tallas, marca, tipo)
);
