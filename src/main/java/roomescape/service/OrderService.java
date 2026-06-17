package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Order;
import roomescape.domain.Reservation;
import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order create(Reservation reservation) {
        Order order = new Order(reservation);
        validateOrderIdNotDuplicated(order);
        return orderRepository.save(order);
    }

    private void validateOrderIdNotDuplicated(Order order) {
        if (orderRepository.findByOrderId(order.getOrderId()).isPresent()) {
            throw new RoomescapeException(ErrorType.DUPLICATE_ORDER_ID,
                    "이미 존재하는 주문 번호입니다. 주문 번호: " + order.getOrderId().getValue());
        }
    }
}
