document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const form = document.getElementById('usuarioForm');
  const tableBody = document.getElementById('usuariosTableBody');
  const message = document.getElementById('formMessage');
  const formTitle = document.getElementById('formTitle');
  const saveBtn = document.getElementById('saveBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');
  const passwordInput = document.getElementById('passwordInput');
  const passwordHint = document.getElementById('passwordHint');

  let editingId = null;

  async function loadUsuarios() {
    try {
      const usuarios = await apiRequest('/usuarios');
      document.getElementById('usuariosCount').textContent = usuarios.length;
      document.getElementById('activosCount').textContent = usuarios.filter(u => u.estado === 'ACTIVO').length;
      document.getElementById('inactivosCount').textContent = usuarios.filter(u => u.estado === 'INACTIVO').length;
      renderUsuarios(usuarios);
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  function renderUsuarios(usuarios) {
    tableBody.innerHTML = '';
    usuarios.forEach((usuario) => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${usuario.idUsuario}</td>
        <td>${usuario.nombre} ${usuario.apellido}</td>
        <td>${usuario.email}</td>
        <td>${usuario.rol}</td>
        <td><span class="status-badge ${usuario.estado.toLowerCase()}">${usuario.estado}</span></td>
        <td class="table-actions">
          <button class="button tiny" data-action="edit" data-id="${usuario.idUsuario}">Editar</button>
          <button class="button tiny danger" data-action="delete" data-id="${usuario.idUsuario}">Eliminar</button>
        </td>
      `;
      tableBody.appendChild(row);
    });
  }

  function fillForm(usuario) {
    editingId = usuario.idUsuario;
    document.getElementById('usuarioId').value = usuario.idUsuario;
    document.getElementById('nombreInput').value = usuario.nombre;
    document.getElementById('apellidoInput').value = usuario.apellido;
    document.getElementById('emailInput').value = usuario.email;
    document.getElementById('rolInput').value = usuario.rol;
    document.getElementById('estadoInput').value = usuario.estado;
    passwordInput.value = '';
    passwordInput.removeAttribute('required');
    passwordHint.textContent = 'dejar vacío para no cambiar la contraseña';
    formTitle.textContent = 'Editar usuario';
    saveBtn.textContent = 'Actualizar usuario';
    cancelEditBtn.hidden = false;
  }

  function resetForm() {
    editingId = null;
    form.reset();
    document.getElementById('usuarioId').value = '';
    passwordInput.setAttribute('required', '');
    passwordHint.textContent = '(mínimo 6 caracteres)';
    formTitle.textContent = 'Nuevo usuario';
    saveBtn.textContent = 'Guardar usuario';
    cancelEditBtn.hidden = true;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.textContent = '';

    const payload = {
      nombre: document.getElementById('nombreInput').value.trim(),
      apellido: document.getElementById('apellidoInput').value.trim(),
      email: document.getElementById('emailInput').value.trim(),
      rol: document.getElementById('rolInput').value,
      estado: document.getElementById('estadoInput').value
    };

    const password = passwordInput.value;
    if (password) payload.password = password;

    if (editingId !== null && !password) {
      delete payload.password;
    }

    try {
      if (editingId !== null) {
        await apiRequest(`/usuarios/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        message.textContent = 'Usuario actualizado correctamente.';
      } else {
        if (!password) {
          message.textContent = 'La contraseña es obligatoria para un nuevo usuario.';
          message.style.color = '#dc2626';
          return;
        }
        await apiRequest('/usuarios', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        message.textContent = 'Usuario creado correctamente.';
      }
      message.style.color = '#0f766e';
      resetForm();
      await loadUsuarios();
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  });

  tableBody.addEventListener('click', async (event) => {
    const button = event.target.closest('[data-action]');
    if (!button) return;

    const id = button.getAttribute('data-id');

    if (button.dataset.action === 'edit') {
      try {
        const usuario = await apiRequest(`/usuarios/${id}`);
        fillForm(usuario);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
      return;
    }

    if (button.dataset.action === 'delete') {
      const confirmDelete = confirm(`¿Desactivar el usuario ${id}?`);
      if (!confirmDelete) return;

      try {
        await apiRequest(`/usuarios/${id}`, { method: 'DELETE' });
        message.textContent = 'Usuario desactivado correctamente.';
        message.style.color = '#0f766e';
        await loadUsuarios();
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    }
  });

  cancelEditBtn.addEventListener('click', resetForm);

  await loadUsuarios();
});