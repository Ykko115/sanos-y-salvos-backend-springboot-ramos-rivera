# usuario — Microservicio de Autenticación y Perfiles

## 1. Descripción del módulo
Este módulo implementa el microservicio de autenticación y gestión de perfiles de usuario para la plataforma veterinaria "Sanos y Salvos". Permite el registro, inicio de sesión y administración de roles de los usuarios, centralizando la seguridad y el acceso a los recursos del sistema.

## 2. Tecnologías utilizadas
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
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
El servicio se inicia por defecto en el puerto 8081.

## 6. Ejecución de pruebas
```bash
mvn test
```

## 7. Variables de configuración (`src/main/resources/application.properties`)
- `server.port` — Puerto de escucha (por defecto: 8081)
- `spring.datasource.*` — Configuración de la base de datos (URL, usuario, contraseña)

## 8. Estructura de carpetas principal
```
src/
 ├── main/
 │    ├── java/com/microservice/usuario/
 │    │      ├── UsuarioApplication.java
 │    │      ├── config/
 │    │      ├── controller/
 │    │      ├── entitie/
 │    │      │     └── dto/
 │    │      ├── repository/
 │    │      └── service/
 │    └── resources/
 │           ├── application.properties
 │           └── schema.sql
 └── test/
      └── java/com/microservice/usuario/
```
