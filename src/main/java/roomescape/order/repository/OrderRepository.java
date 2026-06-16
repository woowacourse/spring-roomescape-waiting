package roomescape.order.repository;

import org.springframework.stereotype.Repository;
import roomescape.order.domain.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 학습용 인메모리 주문 저장소.
 */
@Repository
public class OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public Order save(Order order) {
        store.put(order.getOrderId(), order);
        return order;
    }

    public Order getByOrderId(String orderId) {
        var order = store.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        return order;
    }

}
