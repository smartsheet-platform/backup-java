FROM maven:3.6.3-openjdk-11 as appbuilder
WORKDIR /project
COPY ./.classpath .
COPY ./dependency-reduced-pom.xml .
COPY ./pom.xml .
RUN mvn dependency:go-offline
COPY ./src/ ./src/
RUN mvn package

FROM openjdk:11.0.8-jdk
WORKDIR /app
COPY --from=appbuilder /project/target/smartsheet-org-backup-1.6.2.jar .
