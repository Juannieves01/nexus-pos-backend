# 1. Etapa de Construcción (Build)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de Ejecución (Run)
FROM openjdk:17-jdk-slim
WORKDIR /app

# Aquí está el TRUCO: Copiamos cualquier .jar generado y lo renombramos a app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Railway usa la variable $PORT - usamos sh para expandirla correctamente
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
