# --- Estágio 1: Build da aplicação (Java 25 JDK) ---
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos da RAIZ do projeto (referenciados a partir do contexto)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copia o código fonte da aplicação
COPY src/ ./src

# Realiza o build e empacotamento
RUN ./mvnw clean package -DskipTests

# --- Estágio 2: Runtime leve e seguro (Java 25 JRE) ---
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Usuário de segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Pasta de logs
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

COPY --from=build /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]