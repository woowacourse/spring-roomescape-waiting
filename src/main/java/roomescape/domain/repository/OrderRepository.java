package roomescape.domain.repository;

import roomescape.domain.Order;

public interface OrderRepository {
    Order getById(String orderId);
    void save(Order order);
}
