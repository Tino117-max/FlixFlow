#!/usr/bin/env bash
# =====================================================================
# FixFlow - Puesta en marcha completa sobre XAMPP.
#   1. Arranca Apache + MySQL de XAMPP.
#   2. Importa la base de datos (si se pasa --db o si no existe).
#   3. Arranca el backend Spring Boot en http://localhost:8080
#
# Frontend (Apache): http://localhost/fixflow/frontend/login.html
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LAMPP="${LAMPP:-/opt/lampp/lampp}"

echo ">> Arrancando XAMPP (Apache + MySQL)..."
sudo "$LAMPP" start

if [[ "${1:-}" == "--db" ]]; then
  "$SCRIPT_DIR/import-db.sh"
fi

"$SCRIPT_DIR/start-backend.sh" "${2:-}"

echo ""
echo "======================================================"
echo " FixFlow disponible en:"
echo "   Frontend : http://localhost/fixflow/frontend/login.html"
echo "   API REST : http://localhost:8080/api"
echo ""
echo " Usuarios de prueba:"
echo "   admin@fixflow.com    / admin123    (ADMIN)"
echo "   tecnico@fixflow.com  / tecnico123  (TECNICO)"
echo "   cliente@fixflow.com  / cliente123  (CLIENTE)"
echo "======================================================"
