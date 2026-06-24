package roomescape.domain.payment;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);

    Order updateStatus(String orderId, OrderStatus status);
}
