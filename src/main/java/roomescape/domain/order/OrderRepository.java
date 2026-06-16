package roomescape.domain.order;

import java.util.Optional;

public interface OrderRepository {

    String insert(Order order);

    Optional<Order> findById(String id);
}
