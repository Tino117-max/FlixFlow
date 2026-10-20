const API_BASE = 'http://localhost:8080/api';

function getToken() {
  return localStorage.getItem('fixflowToken');
}

function escapeHtml(value) {
  if (value === null || value === undefined) return '';
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

async function apiRequest(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers
  });

  const contentType = response.headers.get('content-type');
  const data = contentType && contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    throw new Error(data.message || 'Error en la solicitud');
  }

  return data;
}
