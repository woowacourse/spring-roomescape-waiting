package roomescape.domain.order;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(String orderId);
}
