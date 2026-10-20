#!/usr/bin/env bash
# =====================================================================
# FixFlow - Construye el backend (Spring Boot) y lo ejecuta.
# Requiere: Java 17+ y Maven instalados.
# =====================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_DIR/backend"
JAR="$BACKEND_DIR/target/fixflow-backend-0.0.1-SNAPSHOT.jar"
LOG="$PROJECT_DIR/backend/target/fixflow-backend.log"

cd "$BACKEND_DIR"

if [[ "${1:-}" == "--rebuild" || ! -f "$JAR" ]]; then
  echo ">> Compilando backend con Maven..."
  mvn -q clean package -DskipTests
fi

if pgrep -f "fixflow-backend-0.0.1-SNAPSHOT.jar" >/dev/null 2>&1; then
  echo ">> El backend ya está en ejecución."
  exit 0
fi

echo ">> Iniciando backend FixFlow en http://localhost:8080 ..."
nohup java -jar "$JAR" > "$LOG" 2>&1 &
echo $! > "$BACKEND_DIR/target/fixflow-backend.pid"
echo ">> PID: $(cat "$BACKEND_DIR/target/fixflow-backend.pid")  |  Log: $LOG"
echo ">> Ese proceso debe iniciarse DESPUÉS de arrancar MySQL de XAMPP."
