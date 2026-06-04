#!/bin/bash
set -euo pipefail

# Create databases if they don't already exist
createdb_if_missing() {
  local dbname="$1"
  if psql -tAc "SELECT 1 FROM pg_database WHERE datname='$dbname'" | grep -q 1; then
    echo "Database '$dbname' already exists"
  else
    echo "Creating database '$dbname'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -c "CREATE DATABASE \"$dbname\" OWNER \"$POSTGRES_USER\";"
  fi
}

createdb_if_missing usuario_db
createdb_if_missing mascotas_db
createdb_if_missing reportes_db

echo "Databases ensured."
