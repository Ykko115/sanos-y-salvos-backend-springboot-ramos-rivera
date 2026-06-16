Instrucciones para generar y usar las "semillas" (seed SQL) con Docker

1) Objetivo

Colocar dumps SQL de tus bases locales en este directorio (`db/`) para que PostgreSQL los ejecute automáticamente
al inicializar el volumen de datos en `docker-compose up` (los scripts en `/docker-entrypoint-initdb.d/` se ejecutan sólo
la primera vez que se inicializa el volumen).

2) Generar los dumps desde tu Postgres local

Si estás en Linux y puedes usar el usuario `postgres` del sistema (recomendado):

  sudo -u postgres pg_dump -d mascotas_db -F p -f /tmp/mascotas_db.sql
  sudo -u postgres pg_dump -d reportes_db -F p -f /tmp/reportes_db.sql
  sudo chown $USER:$USER /tmp/mascotas_db.sql /tmp/reportes_db.sql
  mv /tmp/mascotas_db.sql ./db/mascotas.sql
  mv /tmp/reportes_db.sql ./db/reportes.sql

Si necesitas usar credenciales:

  export PGPASSWORD='TU_PASS'
  pg_dump -h localhost -p 5432 -U local_user -d mascotas_db -F p -f db/mascotas.sql
  pg_dump -h localhost -p 5432 -U local_user -d reportes_db -F p -f db/reportes.sql
  unset PGPASSWORD

3) Restaurar/usar las semillas en Docker

Luego de colocar `db/mascotas.sql` y `db/reportes.sql` en este directorio, recrea el volumen para forzar la
ejecución de los scripts de inicialización (ADVERTENCIA: esto borrará datos actuales del volumen):

  docker compose down -v
  docker compose up --build

En la primera inicialización Postgres ejecutará los scripts en `db/` — asegúrate de que tu `init-db.sql` (ya incluido)
crea las bases (`usuario_db`, `mascotas_db`, `reportes_db`) si es necesario. Los archivos `mascotas.sql` y `reportes.sql`
deben contener las instrucciones de creación de tablas y `INSERT` con los datos.

4) Nota sobre idempotencia

Los scripts deben ser idempotentes o pensados para inicializar una base vacía. Si quieres ejecutar migraciones en cada
arranque, usa herramientas como Flyway o Liquibase en lugar de scripts SQL planos.

5) Limpieza de dumps problemáticos

Algunos `pg_dump` incluyen comandos y meta-ciertas marcas (por ejemplo `\restrict`, `\unrestrict`, y varios `SET`)
que pueden impedir una importación limpia con un usuario distinto al superusuario o cuando se importan en caliente.
Si al importar observas errores similares a "unrecognized configuration parameter \"transaction_timeout\"" o errores
por comandos de barra invertida, limpia el dump antes de usarlo en `db/` con este comando:

```bash
sed -e '/^\\restrict/d' \
  -e '/^\\unrestrict/d' \
  -e '/^SET transaction_timeout/d' \
  -e '/^SET idle_in_transaction_session_timeout/d' \
  -e '/^SET lock_timeout/d' \
  -e '/^SET statement_timeout/d' \
  input.sql > input.clean.sql
```

Coloca `input.clean.sql` en `db/` (por ejemplo `reportes_db.clean.sql`) y luego importa ese archivo desde el contenedor o deja que
se ejecute en la inicialización (ver sección 3). Si prefieres no mantener copias limpias en el repo, puedes ejecutar
la limpieza en tu máquina y mover el resultado a `db/`.

6) Nota sobre permisos de solo lectura

En `docker-compose.yml` montamos `./db` como `:ro` en el contenedor para evitar que los contenedores
modifiquen los archivos fuente. Si necesitas generar archivos temporales dentro del contenedor (por ejemplo para
limpiar dumps en sitio), hazlo en un directorio temporal y luego copia el resultado al host.

