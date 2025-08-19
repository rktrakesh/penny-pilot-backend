FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/Penny-Pilot-0.0.1-SNAPSHOT.jar penny-pilot.jar
EXPOSE 7777
ENTRYPOINT ["java", "-jar", "penny-pilot.jar"]diss