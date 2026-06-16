# Sanos y Salvos — Backend Spring Boot (Microservicios)

Monorepo con los microservicios Java del proyecto **Sanos y Salvos**, plataforma para reportar y encontrar mascotas perdidas. Desarrollado por **Nicolás Ramos** y **Alberto Rivera** — Instituto Profesional DUOC UC, FullStack 3.

---

## Arquitectura general

```
                        ┌─────────────────┐
   Clientes/Frontend ──►│  API Gateway    │:8080
                        │  (Spring Cloud) │
                        └────────┬────────┘
                                 │  JWT validado aquí
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                   ▼
        ┌──────────┐     ┌──────────────┐    ┌────────────┐
        │ usuario  │     │   mascotas   │    │  reportes  │
        │  :8081   │     │    :8082     │    │   :8083    │
        └────┬─────┘     └──────┬───────┘    └─────┬──────┘
             │                  │  ──Kafka──►       │
             │            ┌─────▼──────┐            │
             │            │  FastAPI   │            │
             │            │  Matcher   │            │
             │            │   :8000    │            │
             └────────────┴─────┬──────┴────────────┘
                                │
                        ┌───────▼────────┐
                        │  PostgreSQL 15 │:5433
                        │  (3 bases)     │
                        └────────────────┘
```

---

## Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| `apigateway` | 8080 | Puerta de entrada única. Valida JWT, enruta al servicio correcto y aplica circuit breakers (Resilience4j). |
| `usuario` | 8081 | Gestión de usuarios: registro, autenticación JWT, perfil y consulta de mascotas propias. Emite el token JWT en el login. |
| `mascotas` | 8082 | CRUD de mascotas. Publica eventos a Kafka y consulta al motor de coincidencias FastAPI. |
| `reportes` | 8083 | Creación y consulta de reportes de mascotas perdidas/encontradas. Consume eventos de Kafka. |
| `fastapi-matcher` | 8000 | Motor de coincidencias en Python (ver repo dedicado). |

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3, Spring Cloud Gateway |
| Seguridad | Spring Security + JWT (JJWT) |
| Persistencia | Spring Data JPA + PostgreSQL 15 |
| Mensajería | Apache Kafka 7.4 + Zookeeper |
| Resiliencia | Resilience4j (circuit breaker) |
| Infraestructura | Docker + Docker Compose |
| Calidad | SonarQube Community |

---

## Seguridad — JWT

Los tokens JWT son generados por el microservicio `usuario` al hacer login y validados en cada petición por el `apigateway`.

| Parámetro | Valor |
|-----------|-------|
| Algoritmo | HS256 |
| Expiración | **10 minutos** (600 000 ms) |
| Variable de entorno | `SECURITY_JWT_EXPIRATION_MS` |

La expiración se configura vía variable de entorno en `docker-compose.yml` (el valor sobreescribe el `application.properties` interno de la imagen). Tras los 10 minutos el backend rechaza el token con **HTTP 401** y el frontend cierra la sesión automáticamente.

---

## Requisitos previos

- Docker y Docker Compose instalados
- Java 17+ y Maven 3.8+ (solo si se quiere ejecutar cada servicio localmente)
- Cuenta en Docker Hub (para publicar imágenes con `build-push.sh`)

---

## Levantar el proyecto con Docker

### 1. Configurar variables de entorno

```bash
cp .env.example .env
# Editar .env si se quieren cambiar credenciales
```

Variables disponibles en `.env`:

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `DOCKER_HUB_USER` | `nyko115` | Usuario de Docker Hub |
| `IMAGE_TAG` | `latest` | Tag de las imágenes |
| `DB_USER` | `user_adm` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `Nykolas2111` | Contraseña de PostgreSQL |

Variables de entorno adicionales definidas directamente en `docker-compose.yml`:

| Variable | Servicio | Valor | Descripción |
|----------|----------|-------|-------------|
| `SECURITY_JWT_EXPIRATION_MS` | `usuario` | `600000` | Expiración del token JWT en ms (10 min) |
| `SECURITY_JWT_SECRET` | `apigateway` | (fijo) | Clave secreta compartida para firmar/validar JWT |

### 2. Levantar todos los servicios

```bash
docker compose up -d
```

Esto inicia: PostgreSQL, Zookeeper, Kafka, Kafka UI, usuario, mascotas, reportes, apigateway, fastapi-matcher y SonarQube.

### 3. Verificar que todo esté corriendo

```bash
docker compose ps
```

### 4. Detener el entorno

```bash
docker compose down
```

---

## Bases de datos

El script `db/00-create-dbs.sh` crea automáticamente las tres bases de datos al primer inicio:

| Base de datos | Microservicio |
|---------------|---------------|
| `usuario_db` | usuario |
| `mascotas_db` | mascotas |
| `reportes_db` | reportes |

El script `db/20-restore-dumps.sh` restaura los dumps SQL desde `db/*.sql`.

---

## Puertos expuestos

| Servicio | Puerto host |
|----------|-------------|
| API Gateway | 8080 |
| Microservicio usuario | 8081 |
| Microservicio mascotas | 8082 |
| Microservicio reportes | 8083 |
| FastAPI Matcher | 8000 |
| PostgreSQL | 5433 |
| Kafka | 9092 / 29092 |
| Kafka UI | 8090 |
| SonarQube | 9000 |

---

## Ejecutar un microservicio individualmente

```bash
cd mascotas   # o usuario / reportes / apigateway
./mvnw clean spring-boot:run
```

---

## Ejecutar tests

```bash
cd mascotas   # o usuario / reportes
./mvnw test
```

---

## Publicar imágenes a Docker Hub

```bash
bash build-push.sh
```

> Después de publicar nuevas imágenes, reiniciar los contenedores con `docker compose up -d` para que tomen los cambios.

---

## Análisis de calidad con SonarQube

Una vez levantado el docker compose, acceder a `http://localhost:9000` con credenciales `admin / admin`.

Los tres proyectos (`usuario`, `mascotas`, `reportes`) tienen Quality Gates configurados y pasan el análisis con cobertura de tests ≥ 80%.

---

## Estructura del repositorio

```
├── apigateway/           # Spring Cloud Gateway + JWT
├── mascotas/             # Microservicio de mascotas
├── reportes/             # Microservicio de reportes
├── usuario/              # Microservicio de usuarios
├── db/                   # Scripts SQL y dumps iniciales
├── docker-compose.yml    # Entorno completo
├── docker-compose.dev.yml
├── build-push.sh         # Script de build y push a Docker Hub
└── .env.example          # Plantilla de variables de entorno
```

---

## Autores

- **Nicolás Ramos** — [@Ykko115](https://github.com/Ykko115)
- **Alberto Rivera**

Instituto Profesional DUOC UC — Carrera FullStack, 2026.
