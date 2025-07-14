FROM maven:3.8-openjdk-17-slim AS build

# Copier uniquement le pom.xml et télécharger les dépendances
COPY pom.xml /app/
WORKDIR /app
RUN mvn dependency:go-offline -B

# Copier le reste du code
COPY . /app

# Build de l'application sans les tests
RUN mvn clean package -DskipTests

# Image finale légère
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier uniquement le jar
COPY --from=build /app/target/*.jar /app/my-spring-boot-app.jar

EXPOSE 8031
CMD ["java", "-jar", "my-spring-boot-app.jar"]
