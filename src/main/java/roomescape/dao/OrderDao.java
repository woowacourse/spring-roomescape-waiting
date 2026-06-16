package roomescape.dao;

import java.util.Optional;
import roomescape.domain.payment.Order;

public interface OrderDao {
    Order insert(Order order);

    Optional<Order> findByOrderId(String orderId);

    Order update(Order order);
}
