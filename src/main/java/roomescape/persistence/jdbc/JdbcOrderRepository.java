package roomescape.persistence.jdbc;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.order.Order;
import roomescape.persistence.OrderRepository;
import roomescape.persistence.jdbc.dao.OrderDao;

@Repository
@RequiredArgsConstructor
public class JdbcOrderRepository implements OrderRepository {

    private final OrderDao orderDao;

    @Override
    public Order save(Order order) {
        return orderDao.save(order);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return orderDao.findByOrderId(orderId);
    }
}
