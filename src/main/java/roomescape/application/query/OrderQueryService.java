package roomescape.application.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Order;
import roomescape.domain.OrderRepository;
import roomescape.domain.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderQueryService(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    public Order getById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 주문입니다. Id: " + orderId));
    }
}
