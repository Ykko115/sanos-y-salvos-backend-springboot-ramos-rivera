#!/bin/bash
set -euo pipefail

# Restore dumps into their databases if the dump files are present

restore_if_exists() {
  local dumpfile="$1"
  local dbname="$2"
  if [ -f "$dumpfile" ]; then
    echo "Restoring $dumpfile into $dbname"
    sed -e '/^\\restrict/d' \
        -e '/^\\unrestrict/d' \
        -e '/^SET transaction_timeout/d' \
        -e '/^SET idle_in_transaction_session_timeout/d' \
        -e '/^SET lock_timeout/d' \
        -e '/^SET statement_timeout/d' "$dumpfile" | \
      psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname="$dbname"
  else
    echo "Skipping $dumpfile (not found)"
  fi
}

restore_if_exists "/docker-entrypoint-initdb.d/usuario_db.sql" "usuario_db"
restore_if_exists "/docker-entrypoint-initdb.d/mascotas_db.sql" "mascotas_db"
restore_if_exists "/docker-entrypoint-initdb.d/reportes_db.sql" "reportes_db"

echo "Restore scripts completed."
