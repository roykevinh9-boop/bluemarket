'use strict';

const iconPaths = {
  truck: '<path d="M1 4h14v13H1zM15 9h4l4 4v4h-8"/><circle cx="5.5" cy="18" r="2.5"/><circle cx="18.5" cy="18" r="2.5"/>',
  package: '<path d="m12 2 9 5v10l-9 5-9-5V7zM3 7l9 5 9-5m-9 5v10M7.5 4.5l9 5"/>',
  basket: '<path d="M3 9h18l-2 11H5zM8 9l4-6 4 6M8 13l1 3m7-3-1 3"/>',
  cart: '<path d="M2 3h3l2.5 12h12L22 7H6"/><circle cx="9" cy="20" r="1.2"/><circle cx="18" cy="20" r="1.2"/>',
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 4.5 4.5"/>',
  heart: '<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0l-1 1-1-1a5.5 5.5 0 0 0-7.8 7.8l8.8 8.8 8.8-8.8a5.5 5.5 0 0 0 0-7.8Z"/>',
  grid: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  tag: '<path d="M20 3h-7l-9 9a2 2 0 0 0 0 3l5 5a2 2 0 0 0 3 0l9-9V4a1 1 0 0 0-1-1z"/><circle cx="17" cy="7" r="1"/>',
  arrow: '<path d="M4 12h15m-6-6 6 6-6 6"/>',
  chevron: '<path d="m9 5 7 7-7 7"/>',
  card: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20M6 15h3"/>',
  x: '<path d="m6 6 12 12M18 6 6 18"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  menu: '<path d="M3 6h18M3 12h18M3 18h18"/>',
};

function icon(name) {
  return `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${iconPaths[name]}</svg>`;
}

const find = (selector) => document.querySelector(selector);
const byId = new Map([...catalogo, ...productosAnteriores].map((product) => [product.id, product]));
const currency = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const money = (amount) => `S/ ${currency.format(amount / 100)}`;
const delivery = 590;
const catalogIds = new Set(catalogo.map((product) => product.id));
const asset = (id) => globalThis.blueMarketImages?.[id] || `assets/${catalogIds.has(id) ? 'productos/' : ''}${id}.webp`;
const bannerAsset = (id) => globalThis.blueMarketImages?.[`banner-${id}`] || `assets/banners/${id}.webp`;
const panel = find('#panel');
const panelBody = find('#panel-body');
const search = find('#search');

function readLocal(key, fallback) {
  try {
    const value = JSON.parse(localStorage.getItem(`bluemarket-v1-${key}`));
    return Array.isArray(value) ? value : fallback;
  } catch {
    return fallback;
  }
}

function saveLocal(key, value) {
  try {
    localStorage.setItem(`bluemarket-v1-${key}`, JSON.stringify(value));
  } catch {
    // La compra sigue disponible en memoria si el navegador bloquea el almacenamiento.
  }
}

const orderStates = ['Registrado', 'En preparación', 'En camino', 'Entregado'];
const couriers = ['Carlos Mendoza', 'Lucía Ramos'];
const orderDate = new Intl.DateTimeFormat('es-PE', { dateStyle: 'short', timeStyle: 'short', timeZone: 'America/Lima' });

class RegistroPedidos {
  constructor() {
    this.refresh();
  }

  refresh() {
    this.orders = readLocal('orders', this.orders || []);
    this.sold = new Map();
    for (const order of this.orders) {
      for (const item of order.items) {
        if (item.id) this.sold.set(item.id, (this.sold.get(item.id) || 0) + item.quantity);
      }
    }
  }

  stock(id) {
    return Math.max(0, byId.get(id).stock - (this.sold.get(id) || 0));
  }

  async update(change) {
    const transaction = () => {
      this.refresh();
      const orders = change(this.orders);
      try {
        localStorage.setItem('bluemarket-v1-orders', JSON.stringify(orders));
      } catch {
        throw new Error('El navegador bloqueó el guardado. Habilita el almacenamiento local e inténtalo otra vez.');
      }
      this.refresh();
    };
    return navigator.locks ? navigator.locks.request('bluemarket-orders', transaction) : transaction();
  }

  register(order) {
    return this.update((orders) => {
      const requested = new Map();
      for (const item of order.items) requested.set(item.id, (requested.get(item.id) || 0) + item.quantity);
      for (const [id, quantity] of requested) {
        if (quantity > this.stock(id)) throw new Error(`El stock cambió para ${byId.get(id).nombre}. Ajusta el carrito para continuar.`);
      }
      return [order, ...orders];
    });
  }

  advance(id, step, courier) {
    return this.update((orders) => {
      const order = orders.find((entry) => entry.id === id);
      const history = order ? orderHistory(order) : [];
      if (step !== history.length || !orderStates[step]) throw new Error('El pedido cambió de estado. Vuelve a abrirlo.');
      if (step === 1 ? !couriers.includes(courier) : order.courier !== courier) {
        throw new Error('Selecciona el repartidor asignado a este pedido.');
      }
      const updated = { ...order, courier, history: [...history, new Date().toISOString()] };
      return orders.map((entry) => entry.id === id ? updated : entry);
    });
  }
}

function orderHistory(order) {
  return order.history || [order.date];
}

const registry = new RegistroPedidos();

class Carrito {
  constructor(saved) {
    this.items = [];
    for (const item of saved) {
      const product = byId.get(item?.id);
      if (!product || !Number.isInteger(item.quantity) || item.quantity < 1) continue;
      if (product.tallas.length ? !product.tallas.includes(item.size) : item.size !== '') continue;
      const quantity = Math.min(item.quantity, product.stock - this.unitsOf(product.id));
      if (quantity > 0 && !this.items.some((line) => line.id === item.id && line.size === item.size)) {
        this.items.push({ id: item.id, size: item.size, quantity });
      }
    }
  }

  unitsOf(id) {
    return this.items.reduce((count, line) => count + (line.id === id ? line.quantity : 0), 0);
  }

  get count() {
    return this.items.reduce((count, line) => count + line.quantity, 0);
  }

  get subtotal() {
    return this.items.reduce((sum, line) => sum + byId.get(line.id).precio * line.quantity, 0);
  }

  change(id, size, delta) {
    if (delta > 0 && this.unitsOf(id) + delta > registry.stock(id)) return false;
    const index = this.items.findIndex((line) => line.id === id && line.size === size);
    if (index === -1) {
      if (delta > 0) this.items.push({ id, size, quantity: delta });
    } else {
      this.items[index].quantity += delta;
      if (this.items[index].quantity <= 0) this.items.splice(index, 1);
    }
    saveLocal('cart', this.items);
    return true;
  }

  clear() {
    this.items = [];
    saveLocal('cart', this.items);
  }
}

const cart = new Carrito(readLocal('cart', []));
const favorites = new Set(readLocal('favorites', []).filter((id) => byId.has(id)));
let confirming = false;
let activeCourier = couriers[0];
const filters = { category: '', type: '', query: '', sort: 'featured', mode: '', page: 1 };
const productsPerPage = 24;
let toastTimer;
let checkoutDraft;
const discountOf = ({ precio, anterior }) => anterior > precio ? Math.round((anterior - precio) / anterior * 100) : 0;
const campaigns = [
  {
    title: 'Ofertas en electrohogar',
    category: 'Electrohogar',
    style: 'electro',
    products: [
      ['eh-20565068', 'Licuadora Ninja 2 L'],
      ['eh-20623169', 'Freidora Oster 7.5 L'],
      ['eh-15768826', 'Microondas LG 25 L'],
    ],
  },
  {
    title: 'Moda y calzado en oferta',
    category: 'Moda',
    style: 'fashion',
    products: [
      ['tot-153542022', 'Camisa de lino'],
      ['tot-150853933', 'Zapatillas urbanas Redwood'],
      ['pv-102364044', 'Short denim Singular'],
    ],
  },
  {
    title: 'Ofertas para tu despensa',
    category: 'Despensa',
    style: 'pantry',
    products: [
      ['tot-113706444', 'Aceite Primor 900 ml'],
      ['tot-113707306', 'Arroz Costeño 5 kg'],
      ['pv-100383606', 'Fideos Don Vittorio 950 g'],
    ],
  },
];
let currentCampaign = 0;

function escapeHtml(value) {
  const replacements = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
  return String(value).replace(/[&<>"']/g, (character) => replacements[character]);
}

function normalize(value) {
  return value.toLocaleLowerCase('es-PE').normalize('NFD').replace(/\p{Diacritic}/gu, '');
}

function showToast(message) {
  const toast = find('#toast');
  toast.textContent = message;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.textContent = ''; }, 2400);
}

function syncCount() {
  find('#cart-count').textContent = cart.count;
  find('.cart-button').setAttribute('aria-label', `Abrir carrito, ${cart.count} productos`);
}

function renderCategories() {
  find('#categories').innerHTML = categories.map(({ key, name, sample, color }) => {
    const products = catalogo.filter((product) => product.categoria === key);
    const item = products.find((product) => sample.test(product.nombre)) || products[0];
    const image = key === 'Electrohogar' ? bannerAsset('freidora') : asset(item.id);
    return `<button type="button" class="category" data-category="${key}" aria-pressed="false"><span class="category-photo ${color}"><img src="${image}" alt="" width="72" height="72"></span><span>${name}</span></button>`;
  }).join('');
  find('#departments-menu').innerHTML = categories.map(({ key, name }) =>
    `<button type="button" data-category="${key}">${name}</button>`).join('');
}

function showCampaign(index, announce = false) {
  currentCampaign = (index + campaigns.length) % campaigns.length;
  const { title, category, style, products } = campaigns[currentCampaign];
  const cards = products.map(([id, name], position) => {
    const product = byId.get(id);
    const image = position === 1 ? bannerAsset({ electro: 'freidora', fashion: 'zapatillas', pantry: 'arroz' }[style]) : asset(id);
    return `<button type="button" class="campaign-product" data-action="detail" data-id="${id}">
      <img src="${image}" alt="" width="288" height="288">
      <span class="campaign-product-copy">
        <span>${name}</span>
        <strong>${money(product.precio)}</strong>
        <s aria-label="Precio anterior ${money(product.anterior)}">${money(product.anterior)}</s>
      </span>
    </button>`;
  });
  find('#campaign-slide').innerHTML = `<div class="campaign-slide campaign-${style}">
    <div class="campaign-copy">
      <h1>${title}</h1>
      <button type="button" class="campaign-link" data-offer-category="${category}">Ver ofertas ${icon('arrow')}</button>
    </div>
    <div class="campaign-products">${cards.join('')}</div>
  </div>`;
  find('#campaign-dots').querySelectorAll('button').forEach((button, position) => {
    if (position === currentCampaign) button.setAttribute('aria-current', 'true');
    else button.removeAttribute('aria-current');
  });
  if (announce) find('#campaign-status').textContent = title;
}

function renderFilters() {
  const root = find('#filters');
  root.hidden = filters.category !== 'Electrohogar' && (filters.mode !== 'offers' || Boolean(filters.category));
  if (root.hidden) return;
  const isElectro = filters.category === 'Electrohogar';
  const options = isElectro
    ? [...new Set(catalogo.filter((p) => p.categoria === 'Electrohogar' && (filters.mode !== 'offers' || discountOf(p))).map((p) => p.tipo))]
    : categories.filter(({ key }) => catalogo.some((p) => p.categoria === key && discountOf(p)));
  const chips = options.map((option) => {
    const key = isElectro ? option : option.key;
    const label = isElectro ? option : option.name;
    return `<button type="button" data-type="${key}" aria-pressed="${filters.type === key}">${label}</button>`;
  });
  root.innerHTML = `<button type="button" data-type="" aria-pressed="${!filters.type}">Todos</button>${chips.join('')}`;
}

function renderFeaturedDeals() {
  const selections = [
    ['Freidoras de aire', 'Electrohogar'],
    ['', 'Despensa'],
    ['Microondas', 'Electrohogar'],
    ['', 'Moda'],
    ['Aspiradoras', 'Electrohogar'],
  ];
  const featured = selections.map(([type, category]) =>
    catalogo.find((p) => p.categoria === category && (!type || p.tipo === type) && discountOf(p) >= 10)
      || catalogo.find((p) => p.categoria === category && (!type || p.tipo === type) && discountOf(p))
  ).filter(Boolean);
  find('#featured-deals').innerHTML = featured.map(productCard).join('');
}

function productCard(product) {
  const { id, precio, anterior, tallas } = product;
  const nombre = escapeHtml(product.nombre);
  const presentacion = escapeHtml(product.presentacion);
  const favorite = favorites.has(id);
  const discount = discountOf(product);
  return `<article class="product">
    <div class="product-visual">
      <button class="product-photo" data-action="detail" data-id="${id}" aria-label="Ver ${nombre}"><img src="${asset(id)}" alt="" width="320" height="320" loading="lazy"></button>
      ${discount ? `<span class="discount">−${discount}%</span>` : ''}
      <button class="icon-button" data-action="favorite" data-id="${id}" aria-label="Favorito: ${nombre}" aria-pressed="${favorite}">${icon('heart')}</button>
    </div>
    <div class="product-info">
      <h3><button data-action="detail" data-id="${id}">${nombre}</button></h3>
      ${presentacion && presentacion !== 'Unidad' && !product.nombre.includes(presentacion) ? `<p class="unit">${presentacion}</p>` : ''}
      <div class="price ${discount ? 'sale-price' : ''}"><strong>${money(precio)}</strong>${discount ? `<s aria-label="Precio anterior ${money(anterior)}">${money(anterior)}</s>` : ''}</div>
      <button class="primary add" data-action="${tallas.length ? 'detail' : 'add'}" data-id="${id}" ${registry.stock(id) ? '' : 'disabled'}>${registry.stock(id) ? icon('plus') + (tallas.length ? ' Elegir talla' : ' Agregar') : 'Agotado'}</button>
    </div>
  </article>`;
}

function renderCatalog() {
  const query = normalize(filters.query.trim());
  const source = filters.mode === 'favorites' ? [...favorites].map((id) => byId.get(id)) : catalogo;
  const queryWords = query ? query.split(/\s+/) : [];
  const products = source.filter((product) => {
    const categoryName = categories.find(({ key }) => key === product.categoria)?.name;
    const description = normalize([product.nombre, product.presentacion, product.categoria, categoryName, product.marca, product.tipo].join(' '));
    return (!filters.category || filters.category === product.categoria)
      && (!filters.type || filters.type === (filters.category === 'Electrohogar' ? product.tipo : product.categoria))
      && queryWords.every((word) => description.includes(word))
      && (filters.mode !== 'offers' || product.anterior > product.precio)
      && (filters.mode !== 'favorites' || favorites.has(product.id));
  });
  const sorters = {
    low: (a, b) => a.precio - b.precio,
    high: (a, b) => b.precio - a.precio,
    discount: (a, b) => discountOf(b) - discountOf(a),
    name: (a, b) => a.nombre.localeCompare(b.nombre, 'es-PE'),
  };
  if (sorters[filters.sort]) products.sort(sorters[filters.sort]);

  const category = categories.find(({ key }) => key === filters.category);
  const modeTitle = { offers: 'Ofertas', favorites: 'Mis favoritos', all: 'Todo el catálogo' };
  const collectionTitle = filters.mode === 'offers' && category ? `Ofertas en ${category.name.toLowerCase()}` : category?.name;
  find('#catalog-title').textContent = query ? 'Resultados de búsqueda' : collectionTitle || modeTitle[filters.mode] || 'Catálogo';
  const pages = Math.max(1, Math.ceil(products.length / productsPerPage));
  filters.page = Math.min(filters.page, pages);
  const first = (filters.page - 1) * productsPerPage;
  const current = products.slice(first, first + productsPerPage);
  find('#results-count').textContent = products.length > productsPerPage
    ? `${first + 1}–${first + current.length} de ${products.length} productos`
    : `${products.length} ${products.length === 1 ? 'producto' : 'productos'}`;
  find('#products').innerHTML = products.length ? current.map(productCard).join('') : `<div class="empty">
    <h3>${filters.mode === 'favorites' ? 'No hay favoritos para mostrar' : 'No encontramos productos'}</h3>
    <p>${filters.mode === 'favorites' ? 'Toca el corazón de un producto para guardarlo.' : 'Prueba otra búsqueda o cambia los filtros.'}</p>
    <button class="secondary" data-category="">Ver todo el catálogo</button></div>`;
  document.querySelectorAll('.category, .nav-inner [data-category]').forEach((button) => {
    button.setAttribute('aria-pressed', String(button.dataset.category ? button.dataset.category === filters.category : filters.mode === 'all'));
  });
  find('.favorite-button').setAttribute('aria-pressed', String(filters.mode === 'favorites'));
  find('.offers-nav').setAttribute('aria-pressed', String(filters.mode === 'offers'));
  renderFilters();
  renderPagination(pages);
}

function renderPagination(pages) {
  const root = find('#pagination');
  root.hidden = pages < 2;
  if (pages < 2) return;
  const current = filters.page;
  const visible = [...new Set([1, ...[current - 1, current, current + 1].filter((page) => page > 1 && page < pages), pages])];
  const buttons = visible.map((page, index) => {
    const gap = index && page - visible[index - 1] > 1 ? '<span aria-hidden="true">…</span>' : '';
    return `${gap}<button type="button" data-page="${page}" ${page === current ? 'aria-current="page"' : ''} aria-label="Página ${page}">${page}</button>`;
  });
  root.innerHTML = `<button type="button" data-page="${current - 1}" ${current === 1 ? 'disabled' : ''}>Anterior</button>${buttons.join('')}<button type="button" data-page="${current + 1}" ${current === pages ? 'disabled' : ''}>Siguiente</button>`;
}

function scrollToCatalog() {
  find('#catalogo').focus({ preventScroll: true });
  find('#catalogo').scrollIntoView({ block: 'start' });
}

function setCollection(category, mode) {
  const sort = mode === 'offers' ? 'discount' : 'featured';
  Object.assign(filters, { category, mode, type: '', query: '', page: 1, sort });
  find('#sort').value = sort;
  toggleDepartments(false);
  search.value = '';
  renderCatalog();
  scrollToCatalog();
}

function toggleDepartments(open) {
  const menu = find('#departments-menu');
  menu.hidden = !open;
  find('.all-nav').setAttribute('aria-expanded', String(open));
}

function showPanel(mode, title, html) {
  panel.dataset.mode = mode;
  find('#panel-title').textContent = title;
  panelBody.innerHTML = html;
  panelBody.scrollTop = 0;
  if (!panel.open) panel.showModal();
}

function totals(subtotal, fee = delivery, saving = 0) {
  return `<div class="totals">
    ${saving > 0 ? `<div class="saving"><span>Ahorro incluido</span><strong>${money(saving)}</strong></div>` : ''}
    <div><span>Subtotal</span><strong>${money(subtotal)}</strong></div>
    <div><span>Delivery</span><strong>${money(fee)}</strong></div>
    <div class="grand"><span>Total</span><strong>${money(subtotal + fee)}</strong></div>
  </div>`;
}

function showCart() {
  if (!cart.count) {
    showPanel('cart', 'Mi carrito', `<div class="empty"><h3>Tu carrito está vacío</h3><p>Agrega productos del catálogo para iniciar tu pedido.</p><button class="primary" data-action="close">Seguir comprando</button></div>`);
    return;
  }
  const rows = cart.items.map(({ id, size, quantity }) => {
    const product = byId.get(id);
    const itemData = `data-id="${id}" data-size="${escapeHtml(size)}"`;
    const name = escapeHtml(product.nombre);
    return `<div class="cart-row">
      <img src="${asset(id)}" alt="" width="64" height="64">
      <div><strong>${name}</strong>${size ? `<small>Talla ${escapeHtml(size)}</small>` : ''}
        <div class="stepper"><button data-action="decrease" ${itemData} aria-label="Quitar una unidad de ${name}">−</button><span>${quantity}</span><button data-action="increase" ${itemData} aria-label="Agregar una unidad de ${name}" ${cart.unitsOf(id) >= registry.stock(id) ? 'disabled' : ''}>+</button></div>
      </div>
      <div class="cart-right"><strong>${money(product.precio * quantity)}</strong><button class="remove" data-action="remove" ${itemData} aria-label="Eliminar ${name}">Eliminar</button></div>
    </div>`;
  }).join('');
  showPanel('cart', 'Mi carrito', `<div class="cart-list">${rows}</div>${totals(cart.subtotal, delivery, cartSaving())}
    <div class="panel-actions"><button class="secondary" data-action="close">Seguir comprando</button><button class="primary" data-action="checkout">Continuar al pedido ${icon('arrow')}</button></div>`);
}

function showDetail(product) {
  const { id, nombre, presentacion, precio, tallas, anterior } = product;
  const available = Math.max(0, registry.stock(id) - cart.unitsOf(id));
  const sizes = tallas.length ? `<div class="detail-size"><label for="product-size">Talla</label><select id="product-size" name="size" required><option value="">Elige una talla</option>${tallas.map((size) => `<option>${escapeHtml(size)}</option>`).join('')}</select></div>` : '';
  showPanel('detail', nombre, `<div class="detail">
    <img src="${asset(id)}" alt="${escapeHtml(nombre)}" width="320" height="320">
    <div>${presentacion ? `<p class="unit">${escapeHtml(presentacion)}</p>` : ''}
      <div class="price ${anterior > precio ? 'sale-price' : ''}"><strong>${money(precio)}</strong>${anterior > precio ? `<s>${money(anterior)}</s>` : ''}</div><p class="note">${available ? `Disponibles: ${available}` : 'Agotado'}</p>
      <form id="detail-form" data-id="${id}">${sizes}<div class="panel-actions"><button class="primary" ${available ? '' : 'disabled'}>${icon('plus')} Agregar al carrito</button></div></form>
    </div></div>`);
}

function showCheckout() {
  if (!cart.count) return showCart();
  showPanel('checkout', 'Datos de entrega', `<p class="note">Completa un pedido de demostración. Puedes usar datos ficticios.</p>
    <form id="checkout-form">
      <div class="form-fields">
        <label class="wide">Nombre <input name="customer" maxlength="80" required></label>
        <label class="wide">Dirección en Lima <input name="address" maxlength="120" required></label>
        <label>Distrito <input name="district" maxlength="50" required></label>
        <label>Forma de pago <select name="payment"><option value="card">Tarjeta simulada</option><option value="cash">Efectivo contra entrega</option></select></label>
        <label id="cash-field" class="wide" hidden>Monto en efectivo (S/) <input type="number" name="cash" step="0.01" min="${((cart.subtotal + delivery) / 100).toFixed(2)}" disabled></label>
      </div>
      <p id="form-error" class="form-error" role="alert"></p>
      ${totals(cart.subtotal, delivery, cartSaving())}
      <div class="panel-actions"><button type="button" class="secondary" data-action="cart">Volver al carrito</button><button class="primary">Confirmar pedido de prueba</button></div>
    </form>`);
  const form = find('#checkout-form');
  if (checkoutDraft) {
    for (const [name, value] of Object.entries(checkoutDraft)) form.elements.namedItem(name).value = value;
  }
  toggleCash(form);
}

function toggleCash(form) {
  const cash = form.elements.payment.value === 'cash';
  find('#cash-field').hidden = !cash;
  form.elements.cash.required = cash;
  form.elements.cash.disabled = !cash;
}

function orderItems(order) {
  return `<ul>${order.items.map(({ name, size, quantity, subtotal }) => `<li>${quantity} × ${escapeHtml(name)}${size ? ` · Talla ${escapeHtml(size)}` : ''} · ${money(subtotal)}</li>`).join('')}</ul>`;
}

function cartSaving() {
  return cart.items.reduce((sum, { id, quantity }) => {
    const { anterior, precio } = byId.get(id);
    return sum + Math.max(0, anterior - precio) * quantity;
  }, 0);
}

async function confirmOrder(form) {
  if (confirming) return;
  const data = new FormData(form);
  if (['customer', 'address', 'district'].some((key) => !data.get(key)?.trim())) {
    find('#form-error').textContent = 'Completa el nombre, la dirección y el distrito.';
    return;
  }
  if (!cart.count) return showCart();
  const subtotal = cart.subtotal;
  const cash = data.get('payment') === 'cash';
  const change = cash ? Math.round(Number(data.get('cash')) * 100) - subtotal - delivery : null;
  if (cash && (!Number.isFinite(change) || change < 0)) {
    find('#form-error').textContent = 'El monto en efectivo debe cubrir el total del pedido.';
    return;
  }
  const order = {
    id: `BM-${crypto.randomUUID().slice(0, 8).toUpperCase()}`,
    date: new Date().toISOString(),
    items: cart.items.map(({ id, size, quantity }) => ({
      id, name: byId.get(id).nombre, size, quantity, subtotal: byId.get(id).precio * quantity,
    })),
    subtotal,
    saving: cartSaving(),
    delivery,
    payment: cash ? 'Efectivo contra entrega' : 'Tarjeta simulada',
    change,
    customer: data.get('customer').trim(),
    address: data.get('address').trim(),
    district: data.get('district').trim(),
  };
  confirming = true;
  const submit = form.querySelector('button.primary');
  submit.disabled = true;
  try {
    await registry.register(order);
  } catch (error) {
    form.querySelector('#form-error').textContent = error.message;
    return;
  } finally {
    confirming = false;
    submit.disabled = false;
  }
  cart.clear();
  checkoutDraft = undefined;
  syncCount();
  renderCatalog();
  renderFeaturedDeals();
  showPanel('confirmation', 'Pedido registrado', `<div class="receipt"><h3>${order.id}</h3>
    <p class="note">Entrega de demostración en ${escapeHtml(order.district)}.</p>
    ${orderItems(order)}${totals(order.subtotal, order.delivery, order.saving)}
    <p class="note">${order.payment}${cash ? ` · Vuelto ${money(change)}` : ''}</p></div>
    <div class="panel-actions"><button class="secondary" data-action="orders">Mis pedidos</button><button class="primary" data-action="close">Seguir comprando</button></div>`);
}

function progress(order) {
  const history = orderHistory(order);
  return `<ol class="order-progress">${orderStates.map((state, index) =>
    `<li class="${history[index] ? 'reached' : ''}" ${index === history.length - 1 ? 'aria-current="step"' : ''}><span>${state}</span>${history[index] ? `<time datetime="${escapeHtml(history[index])}">${orderDate.format(new Date(history[index]))}</time>` : ''}</li>`
  ).join('')}</ol>`;
}

function orderAddress(order) {
  return [order.address, order.district].filter(Boolean).map(escapeHtml).join(', ');
}

function paymentDetails(order) {
  return `${escapeHtml(order.payment)}${order.change == null ? '' : ` · Vuelto ${money(order.change)}`}`;
}

function showOrders() {
  registry.refresh();
  const history = registry.orders.map((order, index) => `<details class="order-card" ${index === 0 ? 'open' : ''}>
    <summary>${escapeHtml(order.id)} · ${orderStates[orderHistory(order).length - 1]} · ${money(order.subtotal + order.delivery)}</summary>
    ${progress(order)}
    ${orderAddress(order) ? `<p class="unit">Entrega en ${orderAddress(order)}</p>` : ''}
    ${order.courier ? `<p class="unit">Repartidor: ${escapeHtml(order.courier)}</p>` : ''}
    ${orderItems(order)}${totals(order.subtotal, order.delivery, order.saving)}
    <p class="note">${paymentDetails(order)}</p>
    <button class="secondary" data-action="receipt" data-id="${escapeHtml(order.id)}">Ver comprobante de demo</button></details>`).join('');
  showPanel('orders', 'Mis pedidos', history || '<div class="empty"><h3>Aún no tienes pedidos</h3><p>Los pedidos de demostración aparecen aquí después de confirmar la compra.</p></div>');
}

function showProfiles() {
  showPanel('profiles', 'Perfiles de demostración', `<p class="note">Los tres perfiles consultan los pedidos guardados en este navegador.</p>
    <div class="profile-links">
      <button class="secondary" data-action="orders">Cliente · Mis pedidos</button>
      <button class="secondary" data-action="admin">Administrador · Gestionar pedidos</button>
      <button class="secondary" data-action="courier">Repartidor · Entregas asignadas</button>
    </div>`);
}

function showWorklist(mode, message = '') {
  registry.refresh();
  const admin = mode === 'admin';
  const title = admin ? 'Gestión de pedidos' : 'Entregas asignadas';
  const options = couriers.map((courier) => `<option ${courier === activeCourier ? 'selected' : ''}>${courier}</option>`).join('');
  const rows = registry.orders.filter((order) => admin || order.courier === activeCourier).map((order) => {
    const history = orderHistory(order);
    const id = escapeHtml(order.id);
    const state = orderStates[history.length - 1];
    const action = admin && history.length === 1
      ? `<form class="assignment" data-order="${id}"><label>Repartidor<select name="courier" required><option value="">Selecciona</option>${couriers.map((name) => `<option>${name}</option>`).join('')}</select></label><button class="primary">Preparar y asignar</button></form>`
      : !admin && history.length < orderStates.length
        ? `<button class="primary" data-action="advance" data-id="${id}" data-step="${history.length}">${history.length === 2 ? 'Iniciar reparto' : 'Confirmar entrega'}</button>`
        : '';
    return `<article class="work-order"><div class="work-order-head"><h3>${id}</h3><span class="order-state">${state}</span></div>
      ${orderAddress(order) ? `<p>${escapeHtml(order.customer)} · ${orderAddress(order)}</p>` : ''}
      ${order.courier ? `<p class="unit">Repartidor: ${escapeHtml(order.courier)}</p>` : ''}
      ${orderItems(order)}<p><strong>Total ${money(order.subtotal + order.delivery)}</strong></p>${action}</article>`;
  }).join('');
  showPanel(mode, title, `<div class="work-toolbar"><button class="text-button" data-action="profiles">Cambiar perfil</button>
    ${admin ? '' : `<label>Repartidor <select id="active-courier">${options}</select></label>`}</div>
    <p class="work-message" role="status">${escapeHtml(message)}</p>
    ${rows || `<div class="empty"><h3>${admin ? 'Aún no hay pedidos' : 'Aún no tienes entregas asignadas'}</h3></div>`}`);
}

async function advanceOrder(id, step, courier, button) {
  const mode = panel.dataset.mode;
  button.disabled = true;
  try {
    await registry.advance(id, step, courier);
    showWorklist(mode, `Pedido ${id}: ${orderStates[step]}.`);
  } catch (error) {
    showWorklist(mode, error.message);
  }
}

function showReceipt(id) {
  const order = registry.orders.find((entry) => entry.id === id);
  if (!order) return showOrders();
  showPanel('print', 'Comprobante de demostración', `<div class="receipt"><h3>BlueMarket · ${escapeHtml(order.id)}</h3>
    <p>${orderDate.format(new Date(order.date))}</p>
    <p>${orderAddress(order)}</p>${orderItems(order)}
    ${totals(order.subtotal, order.delivery, order.saving)}
    <p>${paymentDetails(order)}</p>
    <p class="note">Pedido académico de demostración.</p></div>
    <div class="panel-actions"><button class="secondary" data-action="orders">Volver a mis pedidos</button><button class="primary" data-action="print">Imprimir</button></div>`);
}

function addProduct(id, size = '') {
  if (!cart.change(id, size, 1)) {
    showToast('Ya agregaste todas las unidades disponibles.');
    return false;
  }
  syncCount();
  showToast(`${byId.get(id).nombre} agregado al carrito`);
  return true;
}

document.addEventListener('click', (event) => {
  const button = event.target.closest('button');
  if (!event.target.closest('.departments, .all-nav')) toggleDepartments(false);
  if (!button) return;
  const { category, type, action, id, size, page, campaignStep, campaignIndex, offerCategory } = button.dataset;
  if (campaignStep !== undefined) return showCampaign(currentCampaign + Number(campaignStep), true);
  if (campaignIndex !== undefined) return showCampaign(Number(campaignIndex), true);
  if (offerCategory !== undefined) return setCollection(offerCategory, 'offers');
  if (page !== undefined) {
    filters.page = Number(page);
    renderCatalog();
    return scrollToCatalog();
  }
  if (category !== undefined) return setCollection(category, 'all');
  if (type !== undefined) {
    filters.type = type;
    filters.page = 1;
    renderCatalog();
    find('#filters').querySelector('[aria-pressed="true"]')?.focus({ preventScroll: true });
    return;
  }
  const actions = {
    cart: showCart,
    close: () => panel.close(),
    orders: showOrders,
    profiles: showProfiles,
    admin: () => showWorklist('admin'),
    courier: () => showWorklist('courier'),
    advance: () => advanceOrder(id, Number(button.dataset.step), activeCourier, button),
    receipt: () => showReceipt(id),
    print: () => window.print(),
    offers: () => setCollection('', 'offers'),
    favorites: () => setCollection('', filters.mode === 'favorites' ? 'all' : 'favorites'),
    checkout: showCheckout,
    departments: () => toggleDepartments(find('#departments-menu').hidden),
    detail: () => showDetail(byId.get(id)),
    add: () => addProduct(id),
    favorite: () => {
      if (favorites.has(id)) favorites.delete(id);
      else favorites.add(id);
      saveLocal('favorites', [...favorites]);
      if (filters.mode === 'favorites') renderCatalog();
      document.querySelectorAll(`[data-action="favorite"][data-id="${id}"]`).forEach((control) => {
        control.setAttribute('aria-pressed', String(favorites.has(id)));
      });
    },
    increase: () => updateLine(id, size, 1, 'increase'),
    decrease: () => updateLine(id, size, -1, 'decrease'),
    remove: () => updateLine(id, size, -cart.items.find((line) => line.id === id && line.size === size).quantity, 'remove'),
  };
  if (actions[action]) actions[action]();
});

function updateLine(id, size, delta, action) {
  const previousScroll = panelBody.scrollTop;
  cart.change(id, size, delta);
  syncCount();
  showCart();
  panelBody.scrollTop = previousScroll;
  const replacement = [...panelBody.querySelectorAll(`[data-action="${action}"]`)].find((button) =>
    button.dataset.id === id && button.dataset.size === size && !button.disabled);
  (replacement || panelBody.querySelector('button:not(:disabled)'))?.focus({ preventScroll: true });
}

document.addEventListener('submit', (event) => {
  const form = event.target;
  if (form.id === 'search-form') {
    event.preventDefault();
    Object.assign(filters, { query: search.value, category: '', mode: '', type: '', page: 1 });
    renderCatalog();
    scrollToCatalog();
  } else if (form.id === 'detail-form') {
    event.preventDefault();
    if (addProduct(form.dataset.id, form.elements.size?.value || '')) panel.close();
  } else if (form.id === 'checkout-form') {
    event.preventDefault();
    confirmOrder(form);
  } else if (form.matches('.assignment')) {
    event.preventDefault();
    advanceOrder(form.dataset.order, 1, form.elements.courier.value, form.querySelector('button'));
  }
});

panelBody.addEventListener('change', (event) => {
  if (event.target.id !== 'active-courier') return;
  activeCourier = event.target.value;
  showWorklist('courier');
});

window.addEventListener('storage', (event) => {
  if (event.key !== 'bluemarket-v1-orders') return;
  registry.refresh();
  renderCatalog();
  renderFeaturedDeals();
  if (panel.open && panel.dataset.mode === 'orders') showOrders();
});

panelBody.addEventListener('input', (event) => {
  const form = event.target.form;
  if (form?.id !== 'checkout-form') return;
  if (event.target.name === 'payment') toggleCash(form);
  checkoutDraft = Object.fromEntries(new FormData(form));
});

panel.addEventListener('click', (event) => {
  if (event.target !== panel) return;
  const rect = panel.getBoundingClientRect();
  if (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom) panel.close();
});

find('#sort').addEventListener('change', (event) => {
  filters.sort = event.target.value;
  filters.page = 1;
  renderCatalog();
});
find('.brand').addEventListener('click', () => {
  Object.assign(filters, { query: '', category: '', mode: '', type: '', sort: 'featured', page: 1 });
  search.value = '';
  find('#sort').value = 'featured';
  renderCatalog();
});
document.querySelectorAll('[data-icon]').forEach((element) => { element.innerHTML = icon(element.dataset.icon); });
document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape' && !find('#departments-menu').hidden) {
    toggleDepartments(false);
    find('.all-nav').focus();
  }
});
find('#campaign-dots').innerHTML = campaigns.map(({ title }, index) =>
  `<button type="button" data-campaign-index="${index}" aria-controls="campaign-slide" aria-label="${title}"></button>`
).join('');
find('.campaign').addEventListener('keydown', (event) => {
  if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
  event.preventDefault();
  showCampaign(currentCampaign + (event.key === 'ArrowLeft' ? -1 : 1), true);
  if (!event.target.isConnected) find('#campaign-dots [aria-current]').focus();
});
showCampaign(0);
renderCategories();
renderFeaturedDeals();
renderCatalog();
syncCount();
