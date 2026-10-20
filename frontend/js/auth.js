document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async (event) => {
      event.preventDefault();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;
      const message = document.getElementById('message');

      try {
        const response = await apiRequest('/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password })
        });

        localStorage.setItem('fixflowToken', response.token);
        localStorage.setItem('fixflowUser', JSON.stringify({
          email: response.email,
          rol: response.rol,
          id: response.idUsuario
        }));

        window.location.href = 'dashboard.html';
      } catch (error) {
        message.textContent = error.message;
      }
    });
  }

  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (event) => {
      event.preventDefault();
      localStorage.removeItem('fixflowToken');
      localStorage.removeItem('fixflowUser');
      window.location.href = 'login.html';
    });
  }

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  const allowedRoles = document.body.dataset.roles;
  if (allowedRoles && !user) {
    window.location.href = 'login.html';
    return;
  }
  if (allowedRoles && !allowedRoles.split(',').includes(user.rol)) {
    window.location.href = 'dashboard.html';
    return;
  }

  document.querySelectorAll('[data-roles]').forEach((element) => {
    if (element === document.body) return;
    if (user && element.dataset.roles.split(',').includes(user.rol)) return;
    element.remove();
  });

  const nav = document.querySelector('header nav') || document.querySelector('nav');
  if (allowedRoles && nav && user && user.rol === 'ADMIN' && !nav.querySelector('a[href="clientes.html"]')) {
    const link = document.createElement('a');
    link.href = 'clientes.html';
    link.textContent = 'Clientes';
    const usuariosLink = nav.querySelector('a[href="usuarios.html"]');
    if (usuariosLink) {
      usuariosLink.insertAdjacentElement('afterend', link);
    } else {
      const equiposLink = nav.querySelector('a[href="equipos.html"]');
      if (equiposLink) {
        equiposLink.insertAdjacentElement('beforebegin', link);
      } else {
        nav.prepend(link);
      }
    }
  }

  const roleLabel = document.getElementById('currentRole');
  if (roleLabel && user) {
    roleLabel.textContent = `Sesión: ${user.rol}`;
  }
});
