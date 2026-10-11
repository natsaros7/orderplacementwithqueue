# Build context is the repo root (`docker build -f payment-service/Dockerfile .`) so the reactor
# (order-contracts + payment-service) can be built with Maven's -am "also make" flag.
FROM eclipse-temurin:25-jdk AS build
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
RUN java -Djarmode=tools -jar payment-service/target/payment-service-*.jar extract --layers --launcher \
    --destination /workspace/extracted

# eclipse-temurin's Ubuntu-based images already ship curl/wget (used by the compose healthcheck)
# and already have a non-root "ubuntu" user at uid 1000, so a fresh --uid 1000 useradd would clash.
FROM eclipse-temurin:25-jre AS runtime
RUN useradd --system --no-create-home --shell /usr/sbin/nologin app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./
USER app
EXPOSE 8082
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
