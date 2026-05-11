# mascotas — Microservicio de Gestión de Mascotas

## 1. Descripción del módulo
Este módulo implementa el microservicio de gestión de mascotas para la plataforma veterinaria "Sanos y Salvos". Su función principal es ofrecer operaciones CRUD (crear, leer, actualizar y eliminar) sobre los registros de mascotas, permitiendo la administración eficiente de la información asociada a cada animal.

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
El servicio se inicia por defecto en el puerto 8082.

## 6. Ejecución de pruebas
```bash
mvn test
```

## 7. Variables de configuración (`src/main/resources/application.properties`)
- `server.port` — Puerto de escucha (por defecto: 8082)
- `spring.datasource.*` — Configuración de la base de datos (URL, usuario, contraseña)

## 8. Estructura de carpetas principal
```
src/
 ├── main/
 │    ├── java/mascotas/microservice/mascotas/
 │    │      ├── MascotasApplication.java
 │    │      ├── config/
 │    │      ├── controller/
 │    │      ├── entity/
 │    │      ├── repository/
 │    │      └── service/
 │    └── resources/
 │           └── application.properties
 └── test/
      └── java/mascotas/mascotas/
```
