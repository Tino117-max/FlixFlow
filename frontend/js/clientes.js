document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const usuarioSelect = document.getElementById('usuarioSelect');
  const form = document.getElementById('clienteForm');
  const tableBody = document.getElementById('clientesTableBody');
  const message = document.getElementById('formMessage');

  async function loadData() {
    try {
      const clientes = await apiRequest('/clientes');
      const usuarios = await apiRequest('/usuarios');
      usuarioSelect.innerHTML = '<option value="">Seleccione</option>';
      usuarios.forEach((usuario) => {
        const option = document.createElement('option');
        option.value = usuario.idUsuario;
        option.textContent = `${usuario.nombre} ${usuario.apellido} (${usuario.email})`;
        usuarioSelect.appendChild(option);
      });

      document.getElementById('clientesCount').textContent = clientes.length;
      renderClientes(clientes);
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  function renderClientes(clientes) {
    tableBody.innerHTML = '';
    clientes.forEach((cliente) => {
      const row = document.createElement('tr');
      const usuario = cliente.usuario || {};
      row.innerHTML = `
        <td>${cliente.idCliente}</td>
        <td>${escapeHtml(`${usuario.nombre || ''} ${usuario.apellido || ''}`.trim())}</td>
        <td>${escapeHtml(cliente.telefono)}</td>
        <td>${escapeHtml(cliente.direccion)}</td>
      `;
      tableBody.appendChild(row);
    });
  }

  if (form) {
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      message.textContent = '';

      const payload = {
        idUsuario: Number(usuarioSelect.value),
        telefono: document.getElementById('telefonoInput').value.trim(),
        direccion: document.getElementById('direccionInput').value.trim()
      };

      try {
        await apiRequest('/clientes', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        form.reset();
        await loadData();
        message.textContent = 'Cliente creado correctamente.';
        message.style.color = '#0f766e';
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    });
  }

  await loadData();
});
