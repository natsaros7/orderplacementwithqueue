# Build context is the repo root (`docker build -f payment-service/Dockerfile .`) so the reactor
# (order-contracts + payment-service) can be built with Maven's -am "also make" flag.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY order-contracts/pom.xml order-contracts/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
RUN ./mvnw -B -ntp -q -pl payment-service -am dependency:go-offline
COPY order-contracts/src order-contracts/src
COPY payment-service/src payment-service/src
RUN ./mvnw -B -ntp -q -pl payment-service -am -DskipTests package
RUN java -Djarmode=tools -jar payment-service/target/payment-service-*-exec.jar extract --layers --launcher \
    --destination /workspace/extracted

FROM eclipse-temurin:21-jre AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1000 --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./
USER app
EXPOSE 8082
HEALTHCHECK --interval=10s --timeout=5s --retries=10 \
    CMD curl -sf http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
