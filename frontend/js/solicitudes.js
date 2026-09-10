document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  const role = user?.rol;

  const clienteSelect = document.getElementById('clienteSelect');
  const equipoSelect = document.getElementById('equipoSelect');
  const prioridadSelect = document.getElementById('prioridadSelect');
  const descripcionInput = document.getElementById('descripcionInput');
  const form = document.getElementById('solicitudForm');
  const formTitle = document.getElementById('formTitle');
  const saveBtn = document.getElementById('saveBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');
  const message = document.getElementById('formMessage');
  const tableBody = document.getElementById('solicitudesTableBody');

  let editingId = null;

  if (role === 'TECNICO') {
    form.closest('.panel').remove();
  }

  async function loadData() {
    try {
      const solicitudes = await apiRequest('/solicitudes');
      let clientes = [];
      let equipos = [];

      if (role !== 'TECNICO') {
        [clientes, equipos] = await Promise.all([
          apiRequest('/clientes'),
          apiRequest('/equipos')
        ]);
      }

      if (role !== 'TECNICO') {
        populateSelect(clienteSelect, clientes, 'idCliente', (cliente) => `${cliente.usuario?.nombre || 'Cliente'} ${cliente.usuario?.apellido || ''}`);
        populateSelect(equipoSelect, equipos, 'idEquipo', (equipo) => `${equipo.serial} - ${equipo.marca} ${equipo.modelo}`);
      }

      document.getElementById('totalSolicitudes').textContent = solicitudes.length;
      document.getElementById('pendientesCount').textContent = solicitudes.filter(s => s.estado === 'PENDIENTE').length;
      document.getElementById('reparacionCount').textContent = solicitudes.filter(s => s.estado === 'EN_REPARACION').length;
      document.getElementById('finalizadasCount').textContent = solicitudes.filter(s => s.estado === 'FINALIZADA').length;

      renderSolicitudes(solicitudes);
    } catch (error) {
      message.textContent = error.message;
    }
  }

  function populateSelect(selectElement, items, idKey, formatter) {
    selectElement.innerHTML = '<option value="">Seleccione</option>';
    items.forEach((item) => {
      const option = document.createElement('option');
      option.value = item[idKey];
      option.textContent = formatter(item);
      selectElement.appendChild(option);
    });
  }

  function renderSolicitudes(solicitudes) {
    tableBody.innerHTML = '';

    solicitudes.forEach((solicitud) => {
      const row = document.createElement('tr');
      const clienteName = solicitud.cliente?.usuario ? `${solicitud.cliente.usuario.nombre} ${solicitud.cliente.usuario.apellido}` : 'Cliente';
      const equipoName = solicitud.equipo ? `${solicitud.equipo.serial} / ${solicitud.equipo.marca}` : 'Equipo';
      const nextActions = role === 'CLIENTE' ? '<span class="muted">Solo seguimiento</span>' : getNextActions(solicitud.estado, solicitud.idSolicitud);
      const editAction = role !== 'TECNICO' ? `
        <button class="button tiny" data-action="edit" data-id="${solicitud.idSolicitud}">Editar</button>
      ` : '';

      row.innerHTML = `
        <td>${solicitud.idSolicitud}</td>
        <td>${clienteName}</td>
        <td>${equipoName}</td>
        <td>${solicitud.prioridad}</td>
        <td><span class="status-badge ${solicitud.estado.toLowerCase()}">${solicitud.estado}</span></td>
        <td class="table-actions">${editAction}${nextActions}</td>
      `;

      tableBody.appendChild(row);
    });
  }

  function getNextActions(currentState, idSolicitud) {
    const transitions = {
      SOLICITUD: ['PENDIENTE'],
      PENDIENTE: ['ASIGNADA'],
      ASIGNADA: ['EN_DIAGNOSTICO'],
      EN_DIAGNOSTICO: ['EN_REPARACION'],
      EN_REPARACION: ['FINALIZADA', 'CANCELADA'],
      FINALIZADA: [],
      CANCELADA: []
    };

    const states = transitions[currentState] || [];
    if (states.length === 0) {
      return '';
    }

    return states.map((estado) => `
      <button class="button tiny" data-state="${estado}" data-id="${idSolicitud}">
        ${estado}
      </button>
    `).join('');
  }

  function fillForm(solicitud) {
    editingId = solicitud.idSolicitud;
    clienteSelect.value = solicitud.cliente?.idCliente || '';
    equipoSelect.value = solicitud.equipo?.idEquipo || '';
    prioridadSelect.value = solicitud.prioridad || '';
    descripcionInput.value = solicitud.descripcion || '';
    formTitle.textContent = 'Editar solicitud';
    saveBtn.textContent = 'Actualizar solicitud';
    cancelEditBtn.hidden = false;
  }

  function resetForm() {
    editingId = null;
    form.reset();
    formTitle.textContent = 'Nueva solicitud';
    saveBtn.textContent = 'Guardar solicitud';
    cancelEditBtn.hidden = true;
  }

  if (form) {
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      message.textContent = '';

      const payload = {
        idCliente: Number(clienteSelect.value),
        idEquipo: Number(equipoSelect.value),
        prioridad: prioridadSelect.value,
        descripcion: descripcionInput.value.trim()
      };

      try {
        if (editingId !== null) {
          await apiRequest(`/solicitudes/${editingId}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
          });
          message.textContent = 'Solicitud actualizada correctamente.';
        } else {
          await apiRequest('/solicitudes', {
            method: 'POST',
            body: JSON.stringify(payload)
          });
          message.textContent = 'Solicitud creada correctamente.';
        }
        message.style.color = '#0f766e';
        resetForm();
        await loadData();
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    });

    cancelEditBtn.addEventListener('click', resetForm);
  }

  document.addEventListener('click', async (event) => {
    const stateButton = event.target.closest('[data-state]');
    if (stateButton) {
      const state = stateButton.getAttribute('data-state');
      const idSolicitud = stateButton.getAttribute('data-id');

      if (!state || !idSolicitud) return;

      try {
        await apiRequest(`/solicitudes/${idSolicitud}/estado?estado=${state}`, {
          method: 'PATCH'
        });
        await loadData();
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
      return;
    }

    const editButton = event.target.closest('[data-action="edit"]');
    if (editButton) {
      const id = editButton.getAttribute('data-id');
      try {
        const solicitud = await apiRequest(`/solicitudes/${id}`);
        fillForm(solicitud);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    }
  });

  await loadData();
});