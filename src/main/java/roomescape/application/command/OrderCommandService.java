package roomescape.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Order;
import roomescape.domain.OrderRepository;

@Service
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;

    public OrderCommandService(
            OrderRepository orderRepository
    ) {
        this.orderRepository = orderRepository;
    }

    public void save(Order order) {
        orderRepository.save(order);
    }

    public void update(Order order) {
        orderRepository.update(order);
    }
}
