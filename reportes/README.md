# reportes — Microservicio de Reportes Clínicos

## 1. Descripción del módulo
Este módulo implementa el microservicio de generación de reportes clínicos veterinarios para la plataforma "Sanos y Salvos". Permite crear, consultar y administrar reportes asociados a las mascotas registradas, facilitando el seguimiento y la gestión clínica dentro del sistema.

## 2. Tecnologías utilizadas
- Java 17
- Spring Boot
- Spring Data JPA
- Lombok
- Maven

## 3. Patrones de diseño implementados
- **Repository**: Abstracción y acceso a datos mediante JPA.
- **Singleton**: Gestión de beans y dependencias a través del contenedor IoC de Spring.

## 4. Requisitos previos
- Java 17 o superior
- Maven 3.8 o superior
- MySQL Server

## 5. Ejecución del módulo
```bash
mvn clean install
mvn spring-boot:run
```
El servicio se inicia por defecto en el puerto 8083.

## 6. Ejecución de pruebas
```bash
mvn test
```

## 7. Variables de configuración (`src/main/resources/application.properties`)
- `server.port` — Puerto de escucha (por defecto: 8083)
- `spring.datasource.*` — Configuración de la base de datos (URL, usuario, contraseña)

## 8. Estructura de carpetas principal
```
src/
 ├── main/
 │    ├── java/com/microservice/reportes/
 │    │      ├── ReportesApplication.java
 │    │      ├── config/
 │    │      ├── controller/
 │    │      ├── entity/
 │    │      ├── repository/
 │    │      └── service/
 │    └── resources/
 │           └── application.properties
 └── test/
      └── java/com/microservice/reportes/
```
