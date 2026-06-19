package roomescape.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import roomescape.domain.Order;

/**
 * 학습용 인메모리 주문 저장소.
 */
@Repository
public class OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    public Order getByOrderId(String orderId) {
        var order = store.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        return order;
    }

    public void confirm(String orderId, String paymentKey) {
        getByOrderId(orderId).confirm(paymentKey);
    }

    public void markUnknown(String orderId) {
        getByOrderId(orderId).markUnknown();
    }

    public Optional<Order> findByReservationId(Long reservationId) {
        return store.values().stream()
                .filter(order -> order.getReservationId().equals(reservationId))
                .findFirst();
    }
}
