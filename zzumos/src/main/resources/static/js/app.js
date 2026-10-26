const API = {
  async req(method, url, body) {
    const r = await fetch(url, { method, credentials: 'same-origin',
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined });
    const data = r.status === 204 ? null : await r.json().catch(() => null);
    if (!r.ok) throw new Error((data && data.error) || 'Error ' + r.status);
    return data;
  },
  get: u => API.req('GET', u),
  post: (u, b) => API.req('POST', u, b)
};
const money = n => 'S/ ' + Number(n).toFixed(2);
const esc = s => String(s ?? '').replace(/[&<>"']/g,
  c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// El carrito vive en el navegador y se envía al servidor al confirmar el pedido.
const Cart = {
  key: 'zzumos_cart',
  all() { try { return JSON.parse(localStorage.getItem(this.key)) || []; } catch (e) { return []; } },
  add(item) { try { localStorage.setItem(this.key, JSON.stringify([...this.all(), item])); } catch (e) {} },
  count() { return this.all().reduce((n, i) => n + i.cantidad, 0); }
};

function toast(msg) {
  const t = document.createElement('div');
  t.className = 'toast'; t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 1800);
}

async function renderNav(activo) {
  let me = null;
  try { me = await API.get('/api/auth/me'); } catch (e) {}
  const nav = document.getElementById('nav');
  nav.className = 'nav';
  nav.innerHTML = `<a class="logo" href="index.html">🥞 ZZUMOS</a>
    <a class="link ${activo === 'inicio' ? 'on' : ''}" href="index.html">Inicio</a>
    <a class="link" href="carrito.html">Carrito (${Cart.count()})</a>` +
    (me ? `<span class="hola">Hola, ${esc(me.nombres)}</span><button class="btn sec" id="salir">Salir</button>`
        : `<a class="btn" href="login.html">Iniciar sesión</a>`);
  const s = document.getElementById('salir');
  if (s) s.onclick = async () => { await API.post('/api/auth/logout'); location.href = 'index.html'; };
  return me;
}
