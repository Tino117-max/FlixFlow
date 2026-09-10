document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  const role = user?.rol;

  const clienteSelect = document.getElementById('clienteSelect');
  const form = document.getElementById('equipoForm');
  const formTitle = document.getElementById('formTitle');
  const saveBtn = document.getElementById('saveBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');
  const tableBody = document.getElementById('equiposTableBody');
  const message = document.getElementById('formMessage');

  let editingId = null;

  async function loadData() {
    try {
      const [clientes, equipos] = await Promise.all([
        apiRequest('/clientes'),
        apiRequest('/equipos')
      ]);

      clienteSelect.innerHTML = '<option value="">Seleccione</option>';
      clientes.forEach((cliente) => {
        const option = document.createElement('option');
        option.value = cliente.idCliente;
        const usuario = cliente.usuario || {};
        option.textContent = `${usuario.nombre || ''} ${usuario.apellido || ''}`.trim() || `Cliente ${cliente.idCliente}`;
        clienteSelect.appendChild(option);
      });

      document.getElementById('equiposCount').textContent = equipos.length;
      renderEquipos(equipos);
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  function renderEquipos(equipos) {
    tableBody.innerHTML = '';
    equipos.forEach((equipo) => {
      const row = document.createElement('tr');
      const cliente = equipo.cliente || {};
      const usuario = cliente.usuario || {};
      const actions = `
        <span class="table-actions">
          <button class="button tiny" data-action="edit" data-id="${equipo.idEquipo}">Editar</button>
          ${role === 'ADMIN' ? `<button class="button tiny danger" data-action="delete" data-id="${equipo.idEquipo}">Eliminar</button>` : ''}
        </span>
      `;
      row.innerHTML = `
        <td>${equipo.idEquipo}</td>
        <td>${usuario.nombre || ''} ${usuario.apellido || ''}</td>
        <td>${equipo.tipo}</td>
        <td>${equipo.marca}</td>
        <td>${equipo.modelo}</td>
        <td>${equipo.serial}</td>
        <td>${actions}</td>
      `;
      tableBody.appendChild(row);
    });
  }

  function fillForm(equipo) {
    editingId = equipo.idEquipo;
    clienteSelect.value = equipo.cliente?.idCliente || '';
    document.getElementById('tipoInput').value = equipo.tipo;
    document.getElementById('marcaInput').value = equipo.marca;
    document.getElementById('modeloInput').value = equipo.modelo;
    document.getElementById('serialInput').value = equipo.serial;
    document.getElementById('descripcionInput').value = equipo.descripcion || '';
    formTitle.textContent = 'Editar equipo';
    saveBtn.textContent = 'Actualizar equipo';
    cancelEditBtn.hidden = false;
  }

  function resetForm() {
    editingId = null;
    form.reset();
    formTitle.textContent = 'Nuevo equipo';
    saveBtn.textContent = 'Guardar equipo';
    cancelEditBtn.hidden = true;
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    message.textContent = '';

    const payload = {
      idCliente: Number(clienteSelect.value),
      tipo: document.getElementById('tipoInput').value.trim(),
      marca: document.getElementById('marcaInput').value.trim(),
      modelo: document.getElementById('modeloInput').value.trim(),
      serial: document.getElementById('serialInput').value.trim(),
      descripcion: document.getElementById('descripcionInput').value.trim()
    };

    try {
      if (editingId !== null) {
        await apiRequest(`/equipos/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        message.textContent = 'Equipo actualizado correctamente.';
      } else {
        await apiRequest('/equipos', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        message.textContent = 'Equipo registrado correctamente.';
      }
      message.style.color = '#0f766e';
      resetForm();
      await loadData();
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
        const equipo = await apiRequest(`/equipos/${id}`);
        fillForm(equipo);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
      return;
    }

    if (button.dataset.action === 'delete') {
      const confirmDelete = confirm(`¿Eliminar el equipo ${id}?`);
      if (!confirmDelete) return;

      try {
        await apiRequest(`/equipos/${id}`, { method: 'DELETE' });
        message.textContent = 'Equipo eliminado correctamente.';
        message.style.color = '#0f766e';
        await loadData();
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    }
  });

  cancelEditBtn.addEventListener('click', resetForm);

  await loadData();
});