# Dockerfile para NexusPOS Backend (Spring Boot)
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Directorio de trabajo
WORKDIR /app

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Etapa final - imagen ligera
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR compilado desde la etapa build
COPY --from=build /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Variables de entorno por defecto (se pueden sobreescribir)
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
