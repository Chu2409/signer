# --- Etapa 1: Compilación (Builder) ---
# Usamos una imagen de Maven que incluye JDK 21 (basado en tu pom.xml)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Establecemos el directorio de trabajo
WORKDIR /app

# 1. Copiamos solo el pom.xml para aprovechar la caché de Docker
# Si el pom.xml no cambia, Docker reutilizará la capa de dependencias
COPY pom.xml .

# 2. Descargamos todas las dependencias
RUN mvn dependency:go-offline

# 3. Copiamos el resto del código fuente
COPY src ./src

# 4. Compilamos la aplicación y creamos el .jar
# Saltamos los tests, ya que deberían correr en una etapa de CI separada
RUN mvn package -DskipTests

# --- Etapa 2: Ejecución (Runner) ---
# Usamos una imagen JRE (Java Runtime) mínima basada en Alpine y Java 21
FROM eclipse-temurin:21-jre-alpine

# Creamos un usuario no-root para correr la aplicación (Buena práctica de seguridad)
RUN addgroup -S spring && adduser -S spring -G spring

# Usamos ese usuario
USER spring:spring

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos el .jar compilado desde la etapa 'builder'
# Tu pom.xml define <artifactId>signer</artifactId> y <version>0.0.1-SNAPSHOT</version>
# Por eso el archivo se llama 'signer-0.0.1-SNAPSHOT.jar'.
# Lo renombramos a 'app.jar' para simplicidad.
COPY --from=builder /app/target/signer-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto por defecto de Spring Boot (8080)
EXPOSE 8080

# El comando para iniciar la aplicación cuando el contenedor arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
