(function () {
  const roleConfig = {
    customer: {
      usersKey: 'campusbite_customer_users',
      sessionKey: 'campusbite_customer_session',
      loginPage: 'login.html',
      dashboardPage: 'dashboard.html',
      roleLabel: 'customer'
    },
    vendor: {
      usersKey: 'campusbite_vendor_users',
      sessionKey: 'campusbite_vendor_session',
      loginPage: 'login.html',
      dashboardPage: 'dashboard.html',
      roleLabel: 'vendor'
    }
  };

  function readStorage(key, fallback) {
    try {
      const raw = localStorage.getItem(key);
      return raw ? JSON.parse(raw) : fallback;
    } catch (error) {
      return fallback;
    }
  }

  function writeStorage(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  }

  function api(path, options = {}) {
    return fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options })
      .then(async (response) => {
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
          throw new Error(data.error || 'Request failed');
        }
        return data;
      });
  }

  function getRoleFromPath() {
    const path = window.location.pathname.toLowerCase();
    if (path.includes('/vendor/')) return 'vendor';
    return 'customer';
  }

  function getUsers(role) {
    return readStorage(roleConfig[role].usersKey, []);
  }

  function saveUsers(role, users) {
    writeStorage(roleConfig[role].usersKey, users);
  }

  function getSession(role) {
    return readStorage(roleConfig[role].sessionKey, null);
  }

  function saveSession(role, user) {
    writeStorage(roleConfig[role].sessionKey, {
      name: user.name || user.fullName || 'User',
      email: user.email || '',
      role: roleConfig[role].roleLabel
    });
  }

  function clearSession(role) {
    localStorage.removeItem(roleConfig[role].sessionKey);
  }

  function register(role, payload) {
    const users = getUsers(role);
    const email = String(payload.email || '').trim().toLowerCase();
    const password = String(payload.password || '');

    if (!email || !password) {
      throw new Error('Email and password are required.');
    }

    if (users.some((user) => (user.email || '').toLowerCase() === email)) {
      throw new Error('An account with this email already exists.');
    }

    const newUser = {
      name: payload.name || payload.fullName || payload.businessName || 'User',
      email,
      password,
      phone: payload.phone || '',
      hostelName: payload.hostelName || '',
      roomNumber: payload.roomNumber || '',
      businessName: payload.businessName || '',
      location: payload.location || ''
    };

    users.push(newUser);
    saveUsers(role, users);
    saveSession(role, newUser);
    return newUser;
  }

  function login(role, email, password) {
    const users = getUsers(role);
    const match = users.find((user) => {
      return (user.email || '').toLowerCase() === String(email || '').trim().toLowerCase()
        && String(user.password || '') === String(password || '');
    });

    if (!match) {
      throw new Error('Invalid email or password.');
    }

    saveSession(role, match);
    return match;
  }

  function requireDashboardSession(role) {
    const session = getSession(role);
    if (!session) {
      window.location.href = roleConfig[role].loginPage;
      return null;
    }
    const userName = document.getElementById('dashboardUserName');
    if (userName) {
      userName.textContent = session.name || session.email || 'Welcome';
    }
    return session;
  }

  function renderMenu(items) {
    const container = document.getElementById('foodMenuList');
    if (!container) return;

    if (!items || !items.length) {
      container.innerHTML = '<p class="small-muted">No menu items are available yet.</p>';
      return;
    }

    container.innerHTML = items.map((item) => {
      const availableText = item.available ? 'Available' : 'Unavailable';
      return `
        <div class="menu-item">
          <div>
            <h4>${item.itemName}</h4>
            <p>${availableText} · ZMW ${Number(item.price || 0).toFixed(2)}</p>
          </div>
          <button type="button" data-menu-id="${item.itemId}">${item.available ? 'Add to basket' : 'Unavailable'}</button>
        </div>
      `;
    }).join('');
  }

  async function loadMenu() {
    try {
      const menu = await api('/api/menu');
      renderMenu(menu);
      return menu;
    } catch (error) {
      const container = document.getElementById('foodMenuList');
      if (container) {
        container.innerHTML = `<p class="small-muted">Could not load menu: ${error.message}</p>`;
      }
      return [];
    }
  }

  async function loadOrders() {
    const container = document.getElementById('ordersList');
    if (!container) return;

    try {
      const orders = await api('/api/orders');
      if (!orders.length) {
        container.innerHTML = '<p class="small-muted">No orders yet.</p>';
        return;
      }

      container.innerHTML = orders.map((order) => {
        const lines = (order.items || []).map((line) => `${line.itemName} × ${line.quantity}`).join(', ');
        return `
          <div class="order-item">
            <h4>Order #${order.orderId}</h4>
            <p><strong>Status:</strong> ${order.status}</p>
            <p><strong>Customer:</strong> ${order.studentName}</p>
            <p><strong>Items:</strong> ${lines || 'No items'}</p>
            <p><strong>Location:</strong> ${order.deliveryLocation || 'Not provided'}</p>
          </div>
        `;
      }).join('');
    } catch (error) {
      container.innerHTML = `<p class="small-muted">Could not load orders: ${error.message}</p>`;
    }
  }

  function bindAuthForms() {
    const customerLoginForm = document.getElementById('customerLoginForm');
    if (customerLoginForm) {
      customerLoginForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const email = document.getElementById('customerEmail')?.value || document.getElementById('email')?.value || '';
        const password = document.getElementById('customerPassword')?.value || document.getElementById('password')?.value || '';
        try {
          login('customer', email, password);
          window.location.href = 'dashboard.html';
        } catch (error) {
          alert(error.message);
        }
      });
    }

    const customerRegisterForm = document.getElementById('customerRegisterForm');
    if (customerRegisterForm) {
      customerRegisterForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const fullName = document.getElementById('fullName')?.value || '';
        const email = document.getElementById('email')?.value || '';
        const password = document.getElementById('password')?.value || '';
        const confirmPassword = document.getElementById('confirmPassword')?.value || '';
        const phone = document.getElementById('phone')?.value || '';

        if (password !== confirmPassword) {
          alert('Password confirmation does not match.');
          return;
        }

        try {
          register('customer', { name: fullName, email, password, phone });
          window.location.href = 'dashboard.html';
        } catch (error) {
          alert(error.message);
        }
      });
    }

    const vendorLoginForm = document.getElementById('vendorLoginForm');
    if (vendorLoginForm) {
      vendorLoginForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const email = document.getElementById('vendorEmail')?.value || document.getElementById('email')?.value || '';
        const password = document.getElementById('vendorPassword')?.value || document.getElementById('password')?.value || '';
        try {
          login('vendor', email, password);
          window.location.href = 'dashboard.html';
        } catch (error) {
          alert(error.message);
        }
      });
    }

    const vendorRegisterForm = document.getElementById('vendorRegisterForm');
    if (vendorRegisterForm) {
      vendorRegisterForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const fullName = document.getElementById('fullName')?.value || '';
        const businessName = document.getElementById('businessName')?.value || '';
        const email = document.getElementById('email')?.value || '';
        const phone = document.getElementById('phone')?.value || '';
        const location = document.getElementById('location')?.value || '';
        const password = document.getElementById('password')?.value || '';
        const confirmPassword = document.getElementById('confirmPassword')?.value || '';

        if (password !== confirmPassword) {
          alert('Password confirmation does not match.');
          return;
        }

        try {
          register('vendor', {
            name: fullName,
            businessName,
            email,
            password,
            phone,
            location
          });
          window.location.href = 'dashboard.html';
        } catch (error) {
          alert(error.message);
        }
      });
    }
  }

  function bindDashboard() {
    const role = getRoleFromPath();
    const dashboardRole = roleConfig[role];
    if (!dashboardRole) return;

    const session = requireDashboardSession(role);
    if (!session) return;

    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
      logoutButton.addEventListener('click', () => {
        clearSession(role);
        window.location.href = dashboardRole.loginPage;
      });
    }

    const browseMenuButton = document.getElementById('browseMenuButton');
    if (browseMenuButton) {
      browseMenuButton.addEventListener('click', async () => {
        await loadMenu();
      });
    }

    const quickAddButtons = document.querySelectorAll('[data-menu-id]');
    quickAddButtons.forEach((button) => {
      button.addEventListener('click', () => {
        const id = Number(button.dataset.menuId);
        const item = document.getElementById('foodMenuList');
        if (item) {
          alert('Menu item selected. Open the main order panel to place an order.');
        }
      });
    });

    const vendorMenuButton = document.getElementById('vendorMenuButton');
    if (vendorMenuButton) {
      vendorMenuButton.addEventListener('click', async () => {
        await loadMenu();
      });
    }

    const refreshOrdersButton = document.getElementById('refreshOrdersButton');
    if (refreshOrdersButton) {
      refreshOrdersButton.addEventListener('click', async () => {
        await loadOrders();
      });
    }

    if (document.getElementById('foodMenuList')) {
      loadMenu();
    }

    if (document.getElementById('ordersList')) {
      loadOrders();
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    bindAuthForms();
    bindDashboard();
  });
})();
