package roomescape.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderRepository;
import roomescape.domain.payment.OrderStatus;

public class FakeOrderRepository implements OrderRepository {

    private final Map<Long, Order> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Order save(Order order) {
        long id = sequence++;
        Order savedOrder = new Order(
                id,
                order.getOrderId(),
                order.getAmount(),
                order.getReservationId(),
                order.getStatus()
        );
        storage.put(id, savedOrder);
        return savedOrder;
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return storage.values().stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findAny();
    }

    @Override
    public Order updateStatus(String orderId, OrderStatus status) {
        Order order = findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문 정보를 찾을 수 없습니다."));
        Order updatedOrder = new Order(
                order.getId(),
                order.getOrderId(),
                order.getAmount(),
                order.getReservationId(),
                status
        );
        storage.put(updatedOrder.getId(), updatedOrder);
        return updatedOrder;
    }
}
