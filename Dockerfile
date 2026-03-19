# Estágio de Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente padrão (podem ser subscritas)
ENV GEMINI_API_KEY=muda-me
ENV JWT_SECRET_KEY=uma-chave-muito-secreta-com-pelo-menos-32-caracteres

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]