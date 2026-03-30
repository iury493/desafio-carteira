# Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml 
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código 
COPY src ./src

# Compila 
RUN mvn clean package -DskipTests

# Execução
FROM eclipse-temurin:17-jre-alpine

# Instala curl para healthcheck
RUN apk add --no-cache curl

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expoe a porta da aplicação
EXPOSE 8080

# Configuração do healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]