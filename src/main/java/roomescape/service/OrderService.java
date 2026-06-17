package roomescape.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import roomescape.domain.Order;
import roomescape.exception.client.PaymentAmountMismatchException;
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

    public void validateAmount(String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
    }

    public Long findReservationId(String orderId) {
        return orderRepository.getByOrderId(orderId).getReservationId();
    }
}
