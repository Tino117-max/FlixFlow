#!/usr/bin/env bash
# =====================================================================
# FixFlow - Importa database/fixflow.sql en MySQL/MariaDB de XAMPP.
# Por defecto usa usuario root sin contraseña (configuración de XAMPP).
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SQL="$PROJECT_DIR/database/fixflow.sql"
MYSQL_BIN="${MYSQL_BIN:-/opt/lampp/bin/mysql}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"

if [[ ! -f "$SQL" ]]; then
  echo "No se encontró $SQL" >&2
  exit 1
fi

echo ">> Importando $SQL ..."
if [[ -n "$DB_PASS" ]]; then
  "$MYSQL_BIN" -u "$DB_USER" -p"$DB_PASS" < "$SQL"
else
  "$MYSQL_BIN" -u "$DB_USER" < "$SQL"
fi
echo ">> Base de datos 'fixflow' importada correctamente."
