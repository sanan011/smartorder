FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY services/config-server/build/libs/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]