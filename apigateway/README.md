# apigateway — BFF para Sanos y Salvos

## 1. Descripción del módulo
Este módulo implementa el Backend For Frontend (BFF) de la plataforma veterinaria "Sanos y Salvos". Su objetivo es centralizar y enrutar todas las peticiones provenientes del frontend React hacia los microservicios internos, aplicando políticas de seguridad JWT y CORS en un único punto de entrada. De esta forma, el frontend interactúa solo con el gateway, sin exponer la topología interna ni los endpoints de los microservicios.

## 2. Tecnologías utilizadas
- Java 17
- Spring Boot
- Spring Security
- Maven

## 3. Patrón de diseño implementado
- **Facade**: El gateway actúa como fachada, ocultando la complejidad y estructura interna de los microservicios al frontend.

## 4. Requisitos previos
- Java 17 o superior
- Maven 3.8 o superior
- MySQL Server

## 5. Ejecución del módulo
```bash
mvn clean install
mvn spring-boot:run
```
El servicio se inicia por defecto en el puerto 8080.

## 6. Ejecución de pruebas
```bash
mvn test
```

## 7. Variables de configuración (`src/main/resources/application.properties`)
- `server.port` — Puerto de escucha (por defecto: 8080)
- `spring.datasource.*` — Configuración de la base de datos (URL, usuario, contraseña)
- `jwt.secret` — Clave secreta para la firma y validación de tokens JWT

## 8. Estructura de carpetas principal
```
src/
 ├── main/
 │    ├── java/com/gateway/apigateway/
 │    │      ├── ApigatewayApplication.java
 │    │      ├── config/
 │    │      └── security/
 │    └── resources/
 │           └── application.properties
 └── test/
      └── java/com/gateway/apigateway/
```
