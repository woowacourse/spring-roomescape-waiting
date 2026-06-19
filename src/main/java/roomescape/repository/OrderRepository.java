package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.domain.payment.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    public Order getByOrderId(String orderId) {
        Order order = store.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        return order;
    }
}