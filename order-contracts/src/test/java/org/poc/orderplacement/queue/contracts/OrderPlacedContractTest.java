package org.poc.orderplacement.queue.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class OrderPlacedContractTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void should_roundTripToJsonAndBack_when_serializingOrderPlaced() {
        final OrderPlaced original = new OrderPlaced(UUID.randomUUID(), "WIDGET", 3, Instant.now());

        final String json = jsonMapper.writeValueAsString(original);
        final OrderPlaced roundTripped = jsonMapper.readValue(json, OrderPlaced.class);

        assertThat(roundTripped).isEqualTo(original);
    }
}
