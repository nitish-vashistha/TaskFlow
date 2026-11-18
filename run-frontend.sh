#!/bin/bash
# TaskFlow Frontend Runner
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR/frontend" || exit 1

echo "Starting TaskFlow React Frontend (Vite)..."
echo "Web UI will be available at: http://localhost:5173"
echo "----------------------------------------------------"

npm run dev
