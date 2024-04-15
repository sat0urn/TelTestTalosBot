FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN ["mvn", "clean"]
RUN ["mvn", "install", "-Dmaven.test.skip=true"]

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/TelTestSpringBot-0.0.1-SNAPSHOT.jar tel_bot.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tel_bot.jar"]