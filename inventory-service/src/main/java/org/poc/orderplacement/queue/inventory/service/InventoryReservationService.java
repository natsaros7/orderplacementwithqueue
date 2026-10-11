package org.poc.orderplacement.queue.inventory.service;

import org.poc.orderplacement.queue.contracts.OrderDestinations;
import org.poc.orderplacement.queue.contracts.OrderPlaced;
import org.poc.orderplacement.queue.inventory.config.JmsConfig;
import org.poc.orderplacement.queue.inventory.domain.InventoryItem;
import org.poc.orderplacement.queue.inventory.domain.InventoryReservation;
import org.poc.orderplacement.queue.inventory.repository.InventoryItemRepository;
import org.poc.orderplacement.queue.inventory.repository.InventoryReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryReservationService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryReservationService(final InventoryItemRepository inventoryItemRepository,
            final InventoryReservationRepository reservationRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    @JmsListener(id = "inventoryOrdersListener", destination = OrderDestinations.ORDERS_TOPIC,
            subscription = JmsConfig.DURABLE_SUBSCRIPTION_NAME)
    public void onOrderPlaced(final OrderPlaced orderPlaced) {
        if (reservationRepository.existsByOrderId(orderPlaced.orderId())) {
            LOG.info("Order {} already reserved, skipping duplicate delivery", orderPlaced.orderId());
            return;
        }

        final InventoryItem item = inventoryItemRepository.findById(orderPlaced.productCode())
                .orElseGet(() -> new InventoryItem(orderPlaced.productCode(), 0));

        final InventoryReservation reservation;
        if (item.hasEnoughStock(orderPlaced.quantity())) {
            item.reserve(orderPlaced.quantity());
            inventoryItemRepository.save(item);
            reservation = InventoryReservation.reserved(orderPlaced.orderId(), orderPlaced.productCode(),
                    orderPlaced.quantity());
            LOG.info("Reserved {} x{} for order {}", orderPlaced.productCode(), orderPlaced.quantity(),
                    orderPlaced.orderId());
        } else {
            reservation = InventoryReservation.rejected(orderPlaced.orderId(), orderPlaced.productCode(),
                    orderPlaced.quantity());
            LOG.warn("Insufficient stock for {} x{}, rejecting order {}", orderPlaced.productCode(),
                    orderPlaced.quantity(), orderPlaced.orderId());
        }
        reservationRepository.save(reservation);
    }
}
