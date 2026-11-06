const state = { menu: [], basket: {}, students: [], orders: [] };
const $ = (selector) => document.querySelector(selector);
const money = (value) => `ZMW ${Number(value || 0).toFixed(2)}`;
const foodEmoji = (name) => {
  const n = (name || "").toLowerCase();
  if (n.includes("chicken")) return "🍗";
  if (n.includes("rice")) return "🍛";
  if (n.includes("bean")) return "🫘";
  if (n.includes("chip") || n.includes("fries")) return "🍟";
  if (n.includes("fish")) return "🐟";
  if (n.includes("drink") || n.includes("juice")) return "🥤";
  return "🍲";
};
function notice(message, error = false) {
  const el = $("#notice"); el.textContent = message; el.className = `notice${error ? " error" : ""}`; el.style.display = "block";
  window.scrollTo({top: 0, behavior: "smooth"});
  setTimeout(() => { el.style.display = "none"; }, 5000);
}
async function api(path, options = {}) {
  const response = await fetch(path, { headers: {"Content-Type":"application/json"}, ...options });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || "Request failed");
  return data;
}
async function loadMenu() {
  state.menu = await api("/api/menu");
  renderMenu(); renderBasket(); renderAdminMenu();
}
function renderMenu() {
  const root = $("#menu");
  if (!state.menu.length) { root.innerHTML = '<p class="small-muted">No menu items yet. The vendor can add meals in Vendor desk.</p>'; return; }
  root.innerHTML = state.menu.map(item => {
    const qty = state.basket[item.itemId] || 0;
    return `<article class="food-card"><div class="food-emoji">${foodEmoji(item.itemName)}</div><h3>${escapeHtml(item.itemName)}</h3><div class="price">${money(item.price)} <small>/ serving</small></div><div class="add-row">${item.available ? `<div class="qty"><button data-minus="${item.itemId}" aria-label="Remove one">−</button><span>${qty}</span><button data-plus="${item.itemId}" aria-label="Add one">+</button></div><button class="add-button" data-add="${item.itemId}">Add +</button>` : '<span class="unavailable">Currently unavailable</span>'}</div></article>`;
  }).join("");
}
function renderBasket() {
  const chosen = state.menu.filter(item => (state.basket[item.itemId] || 0) > 0);
  const root = $("#basket");
  if (!chosen.length) root.innerHTML = '<div class="basket-empty">Choose meals from the menu to get started.</div>';
  else root.innerHTML = chosen.map(item => `<div class="basket-line"><span>${escapeHtml(item.itemName)} <small>× ${state.basket[item.itemId]}</small></span><strong>${money(item.price * state.basket[item.itemId])}</strong></div>`).join("");
  $("#total").textContent = money(chosen.reduce((sum,item) => sum + item.price * state.basket[item.itemId], 0));
}
function renderAdminMenu() {
  $("#admin-menu").innerHTML = state.menu.map(item => `<div class="availability-row"><div><strong>${escapeHtml(item.itemName)}</strong><div class="${item.available ? "available-label" : "unavailable-label"}">${item.available ? "Available" : "Unavailable"} · ${money(item.price)}</div></div><button data-availability="${item.itemId}" data-next="${!item.available}">${item.available ? "Mark unavailable" : "Make available"}</button></div>`).join("") || '<p class="small-muted">No menu items available.</p>';
}
async function loadOrders() {
  state.orders = await api("/api/orders");
  $("#orders").innerHTML = state.orders.map(order => `<article class="order-card"><div class="order-head"><strong>Order #${order.orderId} · ${escapeHtml(order.studentName)}</strong><span class="status ${order.status}">${order.status}</span></div><div class="order-meta">${order.items.map(line => `${escapeHtml(line.itemName)} × ${line.quantity}`).join("<br>")}<br>Delivery: ${escapeHtml(order.deliveryLocation)}<br>Total: <strong>${money(order.totalCost)}</strong>${order.notes ? `<br>Note: ${escapeHtml(order.notes)}` : ""}</div><div class="order-actions">${order.status !== "PREPARING" ? `<button data-status="${order.orderId}" data-value="PREPARING">Mark preparing</button>` : ""}${order.status !== "DELIVERED" ? `<button data-status="${order.orderId}" data-value="DELIVERED">Mark delivered</button>` : ""}${order.status !== "PENDING" ? `<button data-status="${order.orderId}" data-value="PENDING">Reset to pending</button>` : ""}</div></article>`).join("") || '<p class="small-muted">No orders yet. New student orders will appear here.</p>';
}
function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[char]));
}
document.addEventListener("click", async event => {
  const target = event.target.closest("button"); if (!target) return;
  try {
    if (target.dataset.tab) {
      document.querySelectorAll(".tab").forEach(tab => tab.classList.toggle("active", tab === target));
      $("#student-panel").classList.toggle("hidden", target.dataset.tab !== "student");
      $("#vendor-panel").classList.toggle("hidden", target.dataset.tab !== "vendor");
      if (target.dataset.tab === "vendor") await Promise.all([loadOrders(), loadMenu()]);
    }
    if (target.dataset.plus) { const id = target.dataset.plus; state.basket[id] = (state.basket[id] || 0) + 1; renderMenu(); renderBasket(); }
    if (target.dataset.add) { const id = target.dataset.add; state.basket[id] = (state.basket[id] || 0) + 1; renderMenu(); renderBasket(); }
    if (target.dataset.minus) { const id = target.dataset.minus; state.basket[id] = Math.max(0, (state.basket[id] || 0) - 1); renderMenu(); renderBasket(); }
    if (target.id === "refresh" || target.id === "vendor-refresh") { await Promise.all([loadMenu(), loadOrders()]); notice("Menu and orders refreshed."); }
    if (target.dataset.availability) {
      await api(`/api/menu/${target.dataset.availability}/availability`, {method:"PATCH",body:JSON.stringify({isAvailable:target.dataset.next === "true"})});
      await loadMenu(); notice("Menu availability updated.");
    }
    if (target.dataset.status) {
      await api(`/api/orders/${target.dataset.status}/status`, {method:"PATCH",body:JSON.stringify({status:target.dataset.value})});
      await loadOrders(); notice(`Order status updated to ${target.dataset.value}.`);
    }
  } catch (error) { notice(error.message, true); }
});
$("#student-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const selected = state.menu.filter(item => (state.basket[item.itemId] || 0) > 0);
  if (!selected.length) { notice("Please add at least one meal to your basket.", true); return; }
  const student = {name:form.get("name"),phone:form.get("phone"),email:form.get("email"),hostelName:form.get("hostelName"),roomNumber:form.get("roomNumber")};
  const deliveryLocation = String(form.get("deliveryLocation") || "").trim();
  try {
    const savedStudent = await api("/api/students", {method:"POST",body:JSON.stringify(student)});
    const order = await api("/api/orders", {method:"POST",body:JSON.stringify({
      studentId:savedStudent.userId,
      items:selected.map(item => ({foodItemId:item.itemId,quantity:state.basket[item.itemId]})),
      deliveryLocation,
      notes:form.get("notes")
    })});
    state.basket = {}; renderMenu(); renderBasket(); event.currentTarget.reset();
    notice(`Order #${order.orderId} placed successfully. Your order is currently PENDING.`);
    await loadOrders();
  } catch (error) { notice(error.message, true); }
});
$("#food-form").addEventListener("submit", async event => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  try {
    await api("/api/menu", {method:"POST",body:JSON.stringify({itemName:form.get("itemName"),price:Number(form.get("price")),isAvailable:true})});
    event.currentTarget.reset(); await loadMenu(); notice("Menu item added.");
  } catch (error) { notice(error.message, true); }
});
loadMenu().catch(error => notice(`Could not load menu: ${error.message}`, true));
