FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Keep the JVM inside Render's 512 MB free-tier limit
ENV JAVA_TOOL_OPTIONS="-Xmx350m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
