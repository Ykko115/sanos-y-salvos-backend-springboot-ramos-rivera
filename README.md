# sanos-y-salvos-backend-springboot-ramos-rivera
Backend de Springboot para el proyecto Sanos y Salvos de los estudiante Nicolas Ramos y Alberto Rivera del instituto profecional DuocUC
# Sanos y Salvos - Backend 🐾

[![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=java)](https://adoptium.net/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot) [![Maven](https://img.shields.io/badge/Maven-3.8%2B-orange?logo=apachemaven)](https://maven.apache.org/)

## 1. 📝 Descripción general y arquitectura

**Sanos y Salvos** es el backend de una plataforma veterinaria, desarrollado bajo una arquitectura de microservicios desacoplados y un BFF (Backend For Frontend) que centraliza la comunicación con el frontend React. El sistema está compuesto por cuatro módulos Maven independientes:

- **apigateway/**: BFF que enruta y centraliza las peticiones del frontend.
- **usuario/**: Microservicio de autenticación y gestión de perfiles de usuario.
- **mascotas/**: Microservicio CRUD para la gestión de mascotas.
- **reportes/**: Microservicio para la generación de reportes clínicos veterinarios.

Cada microservicio es autónomo, con su propia lógica, base de datos y configuración, facilitando la escalabilidad y el mantenimiento.

## 2. ⚙️ Tecnologías utilizadas

- Java 17+
- Spring Boot 3.x
- Maven 3.8+
- Spring Security
- Spring Data JPA
- Lombok
- MySQL (persistencia)

## 3. 🏗️ Patrones de diseño implementados

- **Repository**: Abstracción de acceso a datos en cada microservicio.
- **Singleton**: Gestión de beans y dependencias mediante el contenedor IoC de Spring.
- **Facade**: El módulo apigateway actúa como fachada (BFF) simplificando la interacción del frontend con los microservicios.

## 4. 🧩 Patrones arquitectónicos

- **Microservicios**: Separación de responsabilidades y despliegue independiente.
- **BFF (Backend For Frontend)**: Capa intermedia entre frontend y microservicios.
- **Arquitectura en capas**: Separación lógica en controladores, servicios, repositorios y entidades.

## 5. 📦 Arquetipos Maven utilizados

- Basado en `maven-archetype-quickstart` y adaptado para proyectos Spring Boot.

## 6. 🚦 Requisitos previos

- Java 17 o superior
- Maven 3.8 o superior
- MySQL Server

## 7. 🚀 Instalación y ejecución

Clona el repositorio y ejecuta cada módulo de forma independiente:

```bash
# Clonar el repositorio
git clone <URL_DEL_REPOSITORIO>
cd sanos-y-salvos-backend-springboot-ramos-rivera

# 1. API Gateway (BFF)
cd apigateway
mvn clean install
mvn spring-boot:run

# 2. Microservicio Usuario
cd ../usuario
mvn clean install
mvn spring-boot:run

# 3. Microservicio Mascotas
cd ../mascotas
mvn clean install
mvn spring-boot:run

# 4. Microservicio Reportes
cd ../reportes
mvn clean install
mvn spring-boot:run
```

Configura las credenciales de base de datos en el archivo `src/main/resources/application.properties` de cada microservicio.

## 8. 🧪 Ejecución de pruebas unitarias

Desde la raíz de cada módulo:

```bash
mvn test
```

## 9. 🗂️ Estructura de carpetas

```
├── apigateway/
│   ├── src/
│   ├── pom.xml
├── usuario/
│   ├── src/
│   ├── pom.xml
├── mascotas/
│   ├── src/
│   ├── pom.xml
├── reportes/
│   ├── src/
│   ├── pom.xml
└── README.md
```

## 10. 👥 Integrantes

- Nicolás Ramos
- Alberto Rivera
- DuocUC — DSY1106 Desarrollo Fullstack III

