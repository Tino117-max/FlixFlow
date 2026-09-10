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

  async function loadData() {
    try {
      const [solicitudes, diagnosticos] = await Promise.all([
        apiRequest('/solicitudes'),
        apiRequest('/diagnosticos')
      ]);

      if (!solicitudes || !Array.isArray(solicitudes)) {
        throw new Error('No se pudieron cargar las solicitudes');
      }

      solicitudSelect.innerHTML = '<option value="">Seleccione</option>';
      solicitudes.forEach((solicitud) => {
        const option = document.createElement('option');
        option.value = solicitud.idSolicitud;
        option.textContent = `Solicitud ${solicitud.idSolicitud} - ${solicitud.estado}`;
        solicitudSelect.appendChild(option);
      });

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
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${diagnostico.idDiagnostico}</td>
        <td>${diagnostico.solicitud?.idSolicitud ?? ''}</td>
        <td>${diagnostico.descripcion}</td>
        <td>${diagnostico.fecha || ''}</td>
      `;
      tableBody.appendChild(row);
    });
  }

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
      await loadData();
      message.textContent = 'Diagnóstico registrado correctamente.';
      message.style.color = '#0f766e';
    } catch (error) {
      message.textContent = error.message;
      message.style.color = '#dc2626';
    }
  });

  await loadData();
});
