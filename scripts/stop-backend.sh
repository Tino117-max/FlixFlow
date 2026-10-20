#!/usr/bin/env bash
# =====================================================================
# FixFlow - Detiene el backend Spring Boot.
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$(dirname "$SCRIPT_DIR")/backend/target/fixflow-backend.pid"

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  kill "$(cat "$PID_FILE")"
  rm -f "$PID_FILE"
  echo ">> Backend detenido."
else
  pkill -f "fixflow-backend-0.0.1-SNAPSHOT.jar" 2>/dev/null && echo ">> Backend detenido." || echo ">> El backend no estaba en ejecución."
fi
