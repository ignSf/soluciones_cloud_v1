# Etapa 1: Compilación de la aplicación con Maven y Java 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar configuración de dependencias y código fuente
COPY pom.xml .
COPY src ./src

# Compilar empaquetando en JAR (omitiendo tests para un despliegue rápido)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el archivo JAR generado
COPY --from=build /app/target/*.jar app.jar

# Render asigna dinámicamente la variable de entorno PORT
ENV PORT=8080
EXPOSE 8080

# Iniciar la aplicación enlazándola al puerto asignado por Render
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
