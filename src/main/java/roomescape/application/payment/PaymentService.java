package roomescape.application.payment;

import org.springframework.stereotype.Service;
import roomescape.application.payment.exception.PaymentAmountMismatchException;
import roomescape.application.payment.model.PaymentConfirmation;
import roomescape.application.payment.model.PaymentResult;
import roomescape.domain.OrderRepository;

/**
 * 결제 승인 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        // TODO: 저장된 주문 금액과 요청 amount 가 다르면 PaymentAmountMismatchException 으로 게이트웨이 호출 '전에' 차단한다.
        var order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }

        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        return paymentGateway.confirm(confirmation);
    }

}
