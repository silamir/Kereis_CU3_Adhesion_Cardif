# syntaxe multi-etapes : build puis image d'execution
#
# IMAGE DE BASE — le point de bascule Silamir / client
#   PAASv3 (client) : base-jdk25-dhi-alpine, depuis le registre interne Kereis.
#   Silamir (dev)   : l'image DHI n'est pas accessible hors du reseau client,
#                     on utilise donc un equivalent public de meme JDK.
# Le passage en integration se fait par cet ARG, sans toucher au reste.
ARG BASE_IMAGE=eclipse-temurin:25-jre-alpine
ARG BUILD_IMAGE=maven:3.9-eclipse-temurin-25-alpine

FROM ${BUILD_IMAGE} AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY contracts ./contracts
RUN mvn -B -DskipTests package

FROM ${BASE_IMAGE}
WORKDIR /app
# Utilisateur non root : exigence de durcissement, alignee sur les images DHI
RUN addgroup -S app 2>/dev/null || true; adduser -S -G app app 2>/dev/null || true
COPY --from=build /build/target/*.jar /app/application.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseZGC"
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
