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

  const tecnicoModal = document.getElementById('tecnicoModal');
  const tecnicosTableBody = document.getElementById('tecnicosTableBody');
  const tecnicoModalSub = document.getElementById('tecnicoModalSub');
  const tecnicoModalMessage = document.getElementById('tecnicoModalMessage');

  const detalleModal = document.getElementById('detalleModal');
  const detalleContent = document.getElementById('detalleContent');
  const detalleId = document.getElementById('detalleId');
  const detalleEstado = document.getElementById('detalleEstado');

  let editingId = null;
  let asignacionSolicitudId = null;

  if (role === 'TECNICO') {
    form.closest('.panel').remove();
  }

  if (role === 'CLIENTE') {
    const clienteField = clienteSelect.closest('label');
    if (clienteField) clienteField.hidden = true;
    clienteSelect.required = false;
  }

  async function loadData() {
    try {
      const solicitudes = await apiRequest('/solicitudes');
      let clientes = [];
      let equipos = [];

      if (role === 'ADMIN') {
        [clientes, equipos] = await Promise.all([
          apiRequest('/clientes'),
          apiRequest('/equipos')
        ]);
        populateSelect(clienteSelect, clientes, 'idCliente', (c) => `${c.usuario?.nombre || 'Cliente'} ${c.usuario?.apellido || ''}`);
        populateSelect(equipoSelect, equipos, 'idEquipo', (e) => `${e.serial} - ${e.marca} ${e.modelo}`);
      } else if (role === 'CLIENTE') {
        equipos = await apiRequest('/equipos');
        populateSelect(equipoSelect, equipos, 'idEquipo', (e) => `${e.serial} - ${e.marca} ${e.modelo}`);
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
      const clienteName = solicitud.cliente?.usuario
        ? `${solicitud.cliente.usuario.nombre} ${solicitud.cliente.usuario.apellido}`
        : 'Cliente';
      const equipoName = solicitud.equipo
        ? `${solicitud.equipo.serial} / ${solicitud.equipo.marca}`
        : 'Equipo';

      let actions = '';
      if (role === 'ADMIN') {
        actions += `<button class="button tiny" data-action="edit" data-id="${solicitud.idSolicitud}">Editar</button>`;
        if (solicitud.estado === 'PENDIENTE' || solicitud.estado === 'SOLICITUD') {
          actions += `<button class="button tiny primary" data-action="assign" data-id="${solicitud.idSolicitud}">Asignar técnico</button>`;
        }
        actions += `<button class="button tiny" data-action="detail" data-id="${solicitud.idSolicitud}">Ver detalle</button>`;
      } else if (role === 'TECNICO') {
        actions += getNextActions(solicitud.estado, solicitud.idSolicitud);
        if (!actions) actions = '<span class="muted">Sin acciones</span>';
      } else {
        actions = `<button class="button tiny" data-action="detail" data-id="${solicitud.idSolicitud}">Ver detalle</button>`;
      }

      row.innerHTML = `
        <td>${solicitud.idSolicitud}</td>
        <td>${escapeHtml(clienteName)}</td>
        <td>${escapeHtml(equipoName)}</td>
        <td>${solicitud.prioridad}</td>
        <td><span class="status-badge ${solicitud.estado.toLowerCase()}">${solicitud.estado}</span></td>
        <td class="table-actions">${actions}</td>
      `;

      tableBody.appendChild(row);
    });
  }

  function getNextActions(currentState, idSolicitud) {
    const transitions = {
      ASIGNADA: ['EN_DIAGNOSTICO'],
      EN_DIAGNOSTICO: ['EN_REPARACION'],
      EN_REPARACION: ['FINALIZADA', 'CANCELADA'],
      FINALIZADA: [],
      CANCELADA: []
    };

    const states = transitions[currentState] || [];
    return states.map((estado) => `
      <button class="button tiny" data-state="${estado}" data-id="${idSolicitud}">
        ${estado.replace('_', ' ')}
      </button>
    `).join('');
  }

  /* ---------- Modal: selección de técnico ---------- */
  function openTecnicoModal(idSolicitud) {
    asignacionSolicitudId = Number(idSolicitud);
    tecnicoModalSub.textContent = `Solicitud #${idSolicitud}. Selecciona el técnico responsable del trabajo.`;
    tecnicoModalMessage.textContent = '';
    tecnicoModal.hidden = false;
    renderTecnicos();
  }

  async function renderTecnicos() {
    try {
      const tecnicos = await apiRequest('/tecnicos');
      const activos = tecnicos.filter((t) => (t.usuario?.estado || 'ACTIVO') === 'ACTIVO');

      tecnicosTableBody.innerHTML = '';
      if (activos.length === 0) {
        tecnicosTableBody.innerHTML = '<tr><td colspan="6" class="muted">No hay técnicos activos registrados.</td></tr>';
        return;
      }

      activos.forEach((tecnico) => {
        const usuario = tecnico.usuario || {};
        const row = document.createElement('tr');
        row.className = 'select-tecnico-row';
        row.innerHTML = `
          <td>${tecnico.idTecnico}</td>
          <td>${escapeHtml(`${usuario.nombre || ''} ${usuario.apellido || ''}`.trim())}</td>
          <td>${escapeHtml(tecnico.especialidad)}</td>
          <td>${escapeHtml(tecnico.telefono || '')}</td>
          <td><span class="status-badge activo">ACTIVO</span></td>
          <td><button class="button tiny primary" data-select-tecnico="${tecnico.idTecnico}">Seleccionar</button></td>
        `;
        tecnicosTableBody.appendChild(row);
      });
    } catch (error) {
      tecnicosTableBody.innerHTML = `<tr><td colspan="6" class="muted">${escapeHtml(error.message)}</td></tr>`;
    }
  }

  tecnicosTableBody.addEventListener('click', async (event) => {
    const button = event.target.closest('[data-select-tecnico]');
    if (!button) return;

    const idTecnico = Number(button.getAttribute('data-select-tecnico'));
    button.disabled = true;
    button.textContent = 'Asignando...';

    try {
      await apiRequest('/asignaciones', {
        method: 'POST',
        body: JSON.stringify({ idSolicitud: asignacionSolicitudId, idTecnico })
      });
      tecnicoModal.hidden = true;
      message.textContent = 'Técnico asignado correctamente.';
      message.style.color = '#0f766e';
      await loadData();
    } catch (error) {
      tecnicoModalMessage.textContent = error.message;
      tecnicoModalMessage.style.color = '#dc2626';
      button.disabled = false;
      button.textContent = 'Seleccionar';
    }
  });

  document.getElementById('tecnicoModalClose').addEventListener('click', () => {
    tecnicoModal.hidden = true;
  });

  /* ---------- Modal: detalle de solicitud ---------- */
  async function openDetalle(idSolicitud) {
    try {
      const d = await apiRequest(`/solicitudes/${idSolicitud}/detalle`);
      detalleId.textContent = `#${d.idSolicitud}`;
      detalleEstado.textContent = d.estado.replace('_', ' ');

      const tecnicoAsignado = d.tecnicoNombre
        ? `<div class="detalle-bloque">
            <h4>Técnico asignado</h4>
            <p><strong>${escapeHtml(d.tecnicoNombre)}</strong></p>
            <p>Especialidad: ${escapeHtml(d.tecnicoEspecialidad || 'N/D')}</p>
            ${d.tecnicoTelefono ? `<p>Teléfono: ${escapeHtml(d.tecnicoTelefono)}</p>` : ''}
            ${d.fechaAsignacion ? `<p>Asignado: ${escapeHtml(d.fechaAsignacion.replace('T', ' ').slice(0, 16))}</p>` : ''}
          </div>`
        : `<div class="detalle-bloque vacio">Sin técnico asignado todavía.</div>`;

      const diagnosticoBloque = d.diagnosticoDescripcion
        ? `<div class="detalle-bloque">
            <h4>Diagnóstico del técnico</h4>
            <p>${escapeHtml(d.diagnosticoDescripcion)}</p>
            ${d.diagnosticoFecha ? `<p class="muted">Registrado: ${escapeHtml(d.diagnosticoFecha.replace('T', ' ').slice(0, 16))}</p>` : ''}
          </div>`
        : `<div class="detalle-bloque vacio">Aún no hay diagnóstico registrado.</div>`;

      const reparacionBloque = d.reparacionSolucion
        ? `<div class="detalle-bloque">
            <h4>Reparación / solución aplicada</h4>
            <p><strong>Solución:</strong> ${escapeHtml(d.reparacionSolucion)}</p>
            ${d.reparacionObservaciones ? `<p><strong>Observaciones:</strong> ${escapeHtml(d.reparacionObservaciones)}</p>` : ''}
            ${d.reparacionFechaInicio ? `<p class="muted">Inicio: ${escapeHtml(d.reparacionFechaInicio.replace('T', ' ').slice(0, 16))}</p>` : ''}
            ${d.reparacionFechaFinalizacion ? `<p class="muted">Fin: ${escapeHtml(d.reparacionFechaFinalizacion.replace('T', ' ').slice(0, 16))}</p>` : ''}
          </div>`
        : `<div class="detalle-bloque vacio">Aún no hay reparación registrada.</div>`;

      detalleContent.innerHTML = `
        <div class="detalle-grid">
          <div class="detalle-item"><span>Prioridad</span><strong>${escapeHtml(d.prioridad)}</strong></div>
          <div class="detalle-item"><span>Fecha de creación</span><strong>${escapeHtml(d.fecha ? d.fecha.replace('T', ' ').slice(0, 16) : 'N/D')}</strong></div>
          <div class="detalle-item"><span>Cliente</span><strong>${escapeHtml(d.clienteNombre || 'N/D')}</strong></div>
          <div class="detalle-item"><span>Equipo</span><strong>${escapeHtml(`${d.equipoMarca || ''} ${d.equipoModelo || ''}`.trim() || 'N/D')}</strong></div>
          <div class="detalle-item"><span>Serial</span><strong>${escapeHtml(d.equipoSerial || 'N/D')}</strong></div>
          <div class="detalle-item"><span>Tipo</span><strong>${escapeHtml(d.equipoTipo || 'N/D')}</strong></div>
          <div class="detalle-item full"><span>Descripción del problema</span><strong>${escapeHtml(d.descripcion || '')}</strong></div>
        </div>
        ${tecnicoAsignado}
        ${diagnosticoBloque}
        ${reparacionBloque}
      `;

      detalleModal.hidden = false;
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  document.getElementById('detalleModalClose').addEventListener('click', () => {
    detalleModal.hidden = true;
  });

  [tecnicoModal, detalleModal].forEach((overlay) => {
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) overlay.hidden = true;
    });
  });

  /* ---------- Formulario ---------- */
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
        prioridad: prioridadSelect.value,
        descripcion: descripcionInput.value.trim()
      };

      if (role === 'ADMIN') {
        payload.idCliente = Number(clienteSelect.value);
      }
      payload.idEquipo = Number(equipoSelect.value);

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
    const assignButton = event.target.closest('[data-action="assign"]');
    if (assignButton) {
      openTecnicoModal(assignButton.getAttribute('data-id'));
      return;
    }

    const detailButton = event.target.closest('[data-action="detail"]');
    if (detailButton) {
      await openDetalle(detailButton.getAttribute('data-id'));
      return;
    }

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
