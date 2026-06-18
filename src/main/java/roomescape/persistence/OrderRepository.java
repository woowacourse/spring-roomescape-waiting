package roomescape.persistence;

import java.util.Optional;
import roomescape.domain.order.Order;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByOrderId(String orderId);
}
