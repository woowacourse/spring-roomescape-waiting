package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;
import roomescape.infrastructure.payment.PaymentConfirmation;
import roomescape.service.dto.result.PaymentResult;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다."));

        if (order.amount().compareTo(amount) != 0) {
            throw new PaymentAmountMismatchException(order.amount(), amount);
        }

        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        return paymentGateway.confirm(confirmation);
    }
}
