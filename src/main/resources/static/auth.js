(function () {
  const roleConfig = {
    customer: {
      usersKey: 'campusbite_customer_users',
      sessionKey: 'campusbite_customer_user',
    },
    vendor: {
      usersKey: 'campusbite_vendor_users',
      sessionKey: 'campusbite_vendor_user',
    }
  };

  function readJson(key, fallback) {
    try {
      const value = localStorage.getItem(key);
      return value ? JSON.parse(value) : fallback;
    } catch (error) {
      return fallback;
    }
  }

  function writeJson(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  }

  window.campusBiteAuth = {
    getUsers(role) {
      return readJson(roleConfig[role].usersKey, []);
    },

    saveUsers(role, users) {
      writeJson(roleConfig[role].usersKey, users);
    },

    getSession(role) {
      return readJson(roleConfig[role].sessionKey, null);
    },

    saveSession(role, user) {
      const safeUser = {
        name: user.name || user.fullName || 'User',
        email: user.email || '',
      };
      writeJson(roleConfig[role].sessionKey, safeUser);
    },

    clearSession(role) {
      localStorage.removeItem(roleConfig[role].sessionKey);
    },

    register(role, details) {
      const users = this.getUsers(role);
      const email = String(details.email || '').trim().toLowerCase();
      const password = String(details.password || '');

      if (!email || !password) {
        throw new Error('Email and password are required.');
      }

      if (users.some((user) => (user.email || '').toLowerCase() === email)) {
        throw new Error('An account with this email already exists.');
      }

      const newUser = {
        name: details.name || details.fullName || 'User',
        email,
        password,
        phone: details.phone || '',
        hostelName: details.hostelName || '',
        roomNumber: details.roomNumber || '',
        businessName: details.businessName || '',
        location: details.location || '',
      };

      users.push(newUser);
      this.saveUsers(role, users);
      this.saveSession(role, newUser);
      return newUser;
    },

    login(role, email, password) {
      const users = this.getUsers(role);
      const match = users.find((user) => {
        return (user.email || '').toLowerCase() === String(email || '').trim().toLowerCase()
          && String(user.password || '') === String(password || '');
      });

      if (!match) {
        throw new Error('Invalid email or password.');
      }

      this.saveSession(role, match);
      return match;
    },

    requireSession(role, fallbackPage) {
      const session = this.getSession(role);
      if (!session) {
        window.location.href = fallbackPage;
        return null;
      }
      return session;
    },
  };
})();
