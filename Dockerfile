FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 10000

# Run app
CMD ["java", "-jar", "target/AgentCrawler-1.0-SNAPSHOT.jar"]
