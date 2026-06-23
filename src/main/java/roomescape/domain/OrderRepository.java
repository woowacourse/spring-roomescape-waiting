package roomescape.domain;

import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    void update(Order order);

    Optional<Order> findById(String orderId);

}
