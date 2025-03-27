package org.poc.order.placement.queue.orderplacementwithqueue.order.repositories;

import org.poc.order.placement.queue.orderplacementwithqueue.order.entites.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
