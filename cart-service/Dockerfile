FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY cart-service/pom.xml cart-service/pom.xml
COPY checkout-service/pom.xml checkout-service/pom.xml
COPY cart-service/src cart-service/src

RUN mvn -pl cart-service -am clean package -DskipTests

FROM eclipse-temurin:25-jdk
WORKDIR /app

COPY --from=build /workspace/cart-service/target/cart-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
