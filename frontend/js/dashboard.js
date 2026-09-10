document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem('fixflowToken');
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  const user = JSON.parse(localStorage.getItem('fixflowUser') || 'null');
  const role = user?.rol;

  try {
    const requests = [apiRequest('/solicitudes')];
    if (role === 'ADMIN') {
      requests.push(apiRequest('/usuarios'), apiRequest('/equipos'));
    } else if (role === 'CLIENTE') {
      requests.push(Promise.resolve([]), apiRequest('/equipos'));
    } else {
      requests.push(Promise.resolve([]), Promise.resolve([]));
    }

    const [solicitudes, usuarios, equipos] = await Promise.all(requests);

    document.getElementById('usuariosCount').textContent = usuarios.length;
    document.getElementById('equiposCount').textContent = equipos.length;
    document.getElementById('totalSolicitudes').textContent = solicitudes.length;
    document.getElementById('solicitudesPendientes').textContent = solicitudes.filter(s => s.estado === 'PENDIENTE').length;
    document.getElementById('solicitudesReparacion').textContent = solicitudes.filter(s => s.estado === 'EN_REPARACION').length;
    document.getElementById('solicitudesFinalizadas').textContent = solicitudes.filter(s => s.estado === 'FINALIZADA').length;

    if (role !== 'ADMIN') {
      document.getElementById('usuariosTableBody').closest('.panel').remove();
    }
    if (role === 'TECNICO') {
      document.getElementById('equiposTableBody').closest('.panel').remove();
    }

    const usuariosBody = document.getElementById('usuariosTableBody');
    usuarios.slice(0, 5).forEach(usuario => {
      const row = document.createElement('tr');
      row.innerHTML = `<td>${usuario.nombre} ${usuario.apellido}</td><td>${usuario.email}</td><td>${usuario.rol}</td>`;
      usuariosBody.appendChild(row);
    });

    const equiposBody = document.getElementById('equiposTableBody');
    equipos.slice(0, 5).forEach(equipo => {
      const row = document.createElement('tr');
      row.innerHTML = `<td>${equipo.serial}</td><td>${equipo.tipo}</td><td>${equipo.marca}</td>`;
      equiposBody.appendChild(row);
    });

    const solicitudesBody = document.getElementById('solicitudesTableBody');
    solicitudes.slice(0, 8).forEach(solicitud => {
      const row = document.createElement('tr');
      row.innerHTML = `<td>${solicitud.idSolicitud}</td><td>${solicitud.cliente ? solicitud.cliente.usuario?.nombre || 'Cliente' : 'Cliente'}</td><td>${solicitud.prioridad}</td><td>${solicitud.estado}</td>`;
      solicitudesBody.appendChild(row);
    });
  } catch (error) {
    alert(error.message);
  }
});
