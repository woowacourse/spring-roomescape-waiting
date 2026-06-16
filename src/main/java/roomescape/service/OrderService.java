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

    public Order create(long amount) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        Order order = new Order(orderId, amount);
        orderRepository.save(order);
        return order;
    }
}
