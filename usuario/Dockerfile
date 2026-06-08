FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
# copy both apigateway and usuario modules into build context so inter-module dependencies can be installed
COPY apigateway ./apigateway
COPY usuario ./usuario
WORKDIR /app/apigateway
RUN mvn -B -DskipTests install
WORKDIR /app/usuario
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/usuario/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
