package roomescape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.OrderPendingCommand;
import roomescape.application.service.result.OrderResult;
import roomescape.application.service.result.PayableOrderResult;
import roomescape.domain.order.Order;
import roomescape.event.publisher.EventPublisher;
import roomescape.exception.EntityNotFoundException;
import roomescape.exception.OrderException;
import roomescape.persistence.OrderRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResult createPendingOrder(OrderPendingCommand command) {
        Order order = Order.pending(
                command.targetId(),
                command.orderType(),
                command.orderName(),
                command.amount()
        );
        return OrderResult.from(orderRepository.save(order));
    }

    @Transactional
    public void failOrder(String orderId) {
        orderRepository.findByOrderId(orderId)
                .filter(Order::isPending)
                .ifPresent(order -> {
                    order.failed();
                    orderRepository.save(order);
                    eventPublisher.publishEvents(order.pullEvents());
                });
    }

    @Transactional
    public void payOrder(String orderId) {
        Order order = findPendingOrder(orderId);

        order.paid();
        orderRepository.save(order);
        eventPublisher.publishEvents(order.pullEvents());
    }

    public PayableOrderResult getPayableOrder(String orderId) {
        return PayableOrderResult.from(findPendingOrder(orderId));
    }

    public OrderResult getOrderResult(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문 정보입니다."));
        return OrderResult.from(order);
    }

    private Order findPendingOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문 정보입니다."));
        if (!order.isPending()) {
            throw new OrderException.AlreadyProcessed();
        }
        return order;
    }
}
