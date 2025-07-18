FROM openjdk:17-jre-slim

# Información del mantenedor
LABEL maintainer="Pokemon API Team <pokemon-api@example.com>"
LABEL description="Pokemon API with Clean Architecture"
LABEL version="1.0.0"

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV JVM_OPTS="-Xms512m -Xmx1g"

# Crear usuario no-root para seguridad
RUN groupadd -r pokemon && useradd -r -g pokemon pokemon

# Crear directorio de trabajo
WORKDIR /app

# Crear directorio para logs
RUN mkdir -p /app/logs && chown -R pokemon:pokemon /app

# Copiar el JAR compilado
COPY target/pokemon-api-1.0.0.jar app.jar

# Cambiar ownership del archivo
RUN chown pokemon:pokemon app.jar

# Cambiar a usuario no-root
USER pokemon

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"] 