# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `order-contracts` module: the `OrderPlaced` wire message, destination/type-id constants, and the
  shared Jackson JMS converter factory, so `inventory-service` and `payment-service` no longer
  depend on `order-service`'s Boot application or JPA entity.
- Durable topic subscriptions for both consumers (unique `spring.jms.client-id` + a stable
  `@JmsListener(subscription = ...)` name), so orders placed while a consumer is offline are
  redelivered on restart instead of being lost.
- Publish-after-commit: `OrderService.placeOrder` is `@Transactional` and publishes only via a
  `@TransactionalEventListener(phase = AFTER_COMMIT)`, closing the window where a message could be
  sent for an order whose transaction then rolls back.
- Idempotent consumers: `InventoryReservation` and `Payment` both have a unique constraint on
  `order_id`; both listeners check for an existing record before processing and skip duplicates.
- Inventory no longer goes negative: stock is seeded via `data.sql`, reservations check available
  stock and record a `REJECTED` reservation (instead of an unconstrained decrement) when insufficient.
- A redelivery policy (3 retries, exponential backoff, tunable via `app.jms.redelivery.*`) on both
  consumers; ActiveMQ's built-in `ActiveMQ.DLQ` receives messages that exhaust retries.
- Publish-failure handling: `OrderPlacedPublisher` catches a JMS send failure (e.g. broker down),
  logs it, and records the order as `PUBLISH_FAILED` (in its own `REQUIRES_NEW` transaction) so the
  UI reflects the real outcome instead of claiming success.
- Bean Validation on the order form with inline error rendering; POST/redirect/GET with a flash
  message showing the new order id.
- Read-only dashboards: inventory-service lists stock and reservations, payment-service lists
  payments.
- Actuator `health`/`info` on all three services with liveness/readiness probes enabled.
- Unit tests (Mockito) for services/listeners including idempotency and insufficient-stock paths,
  `@WebMvcTest` for controllers, `@DataJpaTest` for the unique constraints, `*IT`s per consumer
  against an embedded in-VM ActiveMQ broker covering durability (consumer stopped/restarted),
  redelivery-to-DLQ on an unparseable message, and idempotent redelivery, an order-service `*IT`
  asserting commit-gated publishing, and a contract test exercising the real JMS converter
  (type-id property, JSON round trip, rejection of an unrecognized type id).
- `.github/workflows/ci.yml` (build + test on push/PR) and `.github/dependabot.yml`
  (maven + github-actions, weekly, grouped minor/patch).
- Per-service multi-stage `Dockerfile`s and an updated `docker-compose.yml` using
  `apache/activemq-classic`, with health checks wiring service startup order.

### Changed
- Upgraded to Spring Boot 4.1.1 / Spring Framework 7 / Java 21 / Jackson 3, replacing the
  deprecated `MappingJackson2MessageConverter` with `JacksonJsonMessageConverter`.
- Root POM now inherits `spring-boot-starter-parent` instead of only importing the BOM, so plugin
  versions (surefire, failsafe, compiler) are managed consistently instead of drifting.
- JMS wiring now goes through Boot autoconfiguration and `application.yml` properties instead of
  three near-duplicate hand-rolled `JmsConfig` classes hard-coding `tcp://localhost:61616` and
  `setTrustAllPackages(true)`; durable subscriptions use `spring.jms.subscription-durable: true`
  instead of a custom listener container factory.
- Renamed base package from `org.poc.order.placement.queue.orderplacementwithqueue.*` to
  `org.poc.orderplacement.queue.*`, split by feature (`contracts`, `order`, `inventory`, `payment`).
- Replaced `.properties` config files with `application.yml`, with every environment-dependent
  value as `${ENV_VAR:default}`.

### Fixed
- Fixed the package typo `entites` → entity classes now live under `domain`.
- Removed `System.out` logging in favour of SLF4J with `{}` placeholders.
- Removed the controller's `throws Exception` escape hatch.
- ActiveMQ container healthcheck probed an endpoint that always 401s unauthenticated; now a plain
  TCP check against the OpenWire port.
- `useradd --uid 1000` in the service Dockerfiles clashed with the base image's existing `ubuntu`
  user at that uid; now a plain system user with no explicit uid.
- `docker-compose.yml` set `ACTIVEMQ_ADMIN_LOGIN`/`PASSWORD`, which the `apache/activemq-classic`
  entrypoint ignores; now sets `ACTIVEMQ_CONNECTION_USER`/`PASSWORD` (broker auth), which it reads.

### Removed
- `inventory-service` and `payment-service` no longer depend on the `order-service` artifact.
- Removed the unmaintained `rmohr/activemq:5.15.9` (javax-era) image and the obsolete
  `version:` key from `docker-compose.yml`.
