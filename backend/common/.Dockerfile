FROM openjdk:17-jdk-buster AS builder

RUN apt-get update && apt-get install -y findutils && apt-get install dos2unix

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY ./src src
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM openjdk:17-jdk-buster
COPY --from=builder build/libs/*.jar /home/server.jar
EXPOSE 8201
ENTRYPOINT ["java", "-jar", "/home/server.jar", "--spring.profiles.active=dev"]