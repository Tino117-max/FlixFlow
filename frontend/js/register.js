document.addEventListener('DOMContentLoaded', () => {
  const registerForm = document.getElementById('registerForm');
  if (!registerForm) return;

  const message = document.getElementById('message');

  registerForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.textContent = '';

    const nombre = document.getElementById('nombre').value.trim();
    const apellido = document.getElementById('apellido').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const telefono = document.getElementById('telefono').value.trim();
    const direccion = document.getElementById('direccion').value.trim();

    if (password !== confirmPassword) {
      message.textContent = 'Las contraseñas no coinciden.';
      return;
    }

    const submitBtn = registerForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Creando cuenta...';

    try {
      const response = await apiRequest('/auth/registro', {
        method: 'POST',
        body: JSON.stringify({ nombre, apellido, email, password, telefono, direccion })
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
      submitBtn.disabled = false;
      submitBtn.textContent = 'Crear cuenta';
    }
  });
});