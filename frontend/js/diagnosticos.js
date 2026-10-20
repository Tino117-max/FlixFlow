document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const solicitudSelect = document.getElementById('solicitudSelect');
  const form = document.getElementById('diagnosticoForm');
  const tableBody = document.getElementById('diagnosticosTableBody');
  const message = document.getElementById('formMessage');

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  if (user?.rol !== 'TECNICO' && form) {
    form.closest('.panel').remove();
  }

  let solicitudesCache = [];

  function nombreCliente(solicitud) {
    const u = solicitud?.cliente?.usuario;
    return u ? `${u.nombre || ''} ${u.apellido || ''}`.trim() : 'Cliente';
  }

  function nombreEquipo(equipo) {
    if (!equipo) return 'Equipo';
    return `${equipo.marca || ''} ${equipo.modelo || ''} (${equipo.serial || 'sin serial'})`.trim();
  }

  function mostrarProblema() {
    const panel = document.getElementById('problemaInfo');
    if (!panel) return;
    const solicitud = solicitudesCache.find((s) => String(s.idSolicitud) === solicitudSelect.value);
    if (!solicitud) {
      panel.hidden = true;
      return;
    }
    const equipo = solicitud.equipo || {};
    document.getElementById('infoCliente').textContent = nombreCliente(solicitud);
    document.getElementById('infoEquipo').textContent = nombreEquipo(equipo);
    document.getElementById('infoSerial').textContent = equipo.serial || 'N/D';
    document.getElementById('infoPrioridad').textContent = solicitud.prioridad || 'N/D';
    document.getElementById('infoEquipoDesc').textContent = equipo.descripcion || 'Sin descripción del equipo.';
    document.getElementById('infoProblema').textContent = solicitud.descripcion || 'Sin descripción del problema.';
    panel.hidden = false;
  }

  solicitudSelect.addEventListener('change', mostrarProblema);

  async function loadData() {
    try {
      const [solicitudes, diagnosticos] = await Promise.all([
        apiRequest('/solicitudes'),
        apiRequest('/diagnosticos')
      ]);

      if (!solicitudes || !Array.isArray(solicitudes)) {
        throw new Error('No se pudieron cargar las solicitudes');
      }

      solicitudesCache = solicitudes;

      solicitudSelect.innerHTML = '<option value="">Seleccione</option>';
      solicitudes
        .filter((solicitud) => solicitud.estado === 'ASIGNADA')
        .forEach((solicitud) => {
          const option = document.createElement('option');
          option.value = solicitud.idSolicitud;
          option.textContent = `Solicitud ${solicitud.idSolicitud} - ${nombreEquipo(solicitud.equipo)}`;
          option.title = solicitud.descripcion || '';
          solicitudSelect.appendChild(option);
        });
      mostrarProblema();

      document.getElementById('diagnosticosCount').textContent = diagnosticos.length;
      renderDiagnosticos(diagnosticos);
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  function renderDiagnosticos(diagnosticos) {
    tableBody.innerHTML = '';
    diagnosticos.forEach((diagnostico) => {
      const solicitud = diagnostico.solicitud || {};
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${diagnostico.idDiagnostico}</td>
        <td>${solicitud.idSolicitud ?? ''}</td>
        <td>${escapeHtml(nombreEquipo(solicitud.equipo))}</td>
        <td>${escapeHtml(solicitud.descripcion || 'N/D')}</td>
        <td>${escapeHtml(diagnostico.descripcion)}</td>
        <td>${escapeHtml(diagnostico.fecha || '')}</td>
      `;
      tableBody.appendChild(row);
    });
  }

  if (form) {
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      message.textContent = '';

      const payload = {
        idSolicitud: Number(solicitudSelect.value),
        descripcion: document.getElementById('descripcionInput').value.trim()
      };

      try {
        await apiRequest('/diagnosticos', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        form.reset();
        mostrarProblema();
        await loadData();
        message.textContent = 'Diagnóstico registrado correctamente.';
        message.style.color = '#0f766e';
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    });
  }

  await loadData();
});
