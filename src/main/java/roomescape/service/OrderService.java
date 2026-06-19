package roomescape.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import roomescape.domain.Order;
import roomescape.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order create(long amount, Long reservationId) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        Order order = new Order(orderId, amount, reservationId);
        orderRepository.save(order);
        return order;
    }
}
