FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system stackfolio \
    && useradd --system --gid stackfolio --home-dir /app stackfolio

COPY --from=build /workspace/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""

EXPOSE 8080

USER stackfolio

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
