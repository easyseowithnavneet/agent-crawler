FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 10000

# Run app
CMD ["java", "-jar", "target/AgentCrawler-1.0-SNAPSHOT.jar"]
