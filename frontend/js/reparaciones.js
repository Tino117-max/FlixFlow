document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const solicitudSelect = document.getElementById('solicitudSelect');
  const form = document.getElementById('reparacionForm');
  const tableBody = document.getElementById('reparacionesTableBody');
  const message = document.getElementById('formMessage');

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  if (user?.rol !== 'TECNICO' && form) {
    form.closest('.panel').remove();
  }

  async function loadData() {
    try {
      const [solicitudes, reparaciones] = await Promise.all([
        apiRequest('/solicitudes'),
        apiRequest('/reparaciones')
      ]);

      if (!solicitudes || !Array.isArray(solicitudes)) {
        throw new Error('No se pudieron cargar las solicitudes');
      }

      solicitudSelect.innerHTML = '<option value="">Seleccione</option>';
      solicitudes
        .filter((solicitud) => solicitud.estado === 'EN_DIAGNOSTICO')
        .forEach((solicitud) => {
          const option = document.createElement('option');
          option.value = solicitud.idSolicitud;
          option.textContent = `Solicitud ${solicitud.idSolicitud} - ${solicitud.estado}`;
          solicitudSelect.appendChild(option);
        });

      document.getElementById('reparacionesCount').textContent = reparaciones.length;
      renderReparaciones(reparaciones);
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  }

  function renderReparaciones(reparaciones) {
    tableBody.innerHTML = '';
    reparaciones.forEach((reparacion) => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${reparacion.idReparacion}</td>
        <td>${reparacion.solicitud?.idSolicitud ?? ''}</td>
        <td>${escapeHtml(reparacion.solucion)}</td>
        <td>${escapeHtml(reparacion.observaciones || '')}</td>
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
        solucion: document.getElementById('solucionInput').value.trim(),
        observaciones: document.getElementById('observacionesInput').value.trim()
      };

      try {
        await apiRequest('/reparaciones', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        form.reset();
        await loadData();
        message.textContent = 'Reparación registrada correctamente.';
        message.style.color = '#0f766e';
      } catch (error) {
        message.textContent = error.message;
        message.style.color = '#dc2626';
      }
    });
  }

  await loadData();
});
