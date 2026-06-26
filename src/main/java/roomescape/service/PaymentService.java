package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderRepository;
import roomescape.domain.payment.OrderStatus;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentRepository;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.NotFoundException;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.PaymentConfirmationUnknownException;
import roomescape.infra.toss.TossPaymentException;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;

    public PaymentService(OrderRepository orderRepository, PaymentGateway paymentGateway,
                          PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(noRollbackFor = {TossPaymentException.class, PaymentConfirmationUnknownException.class})
    public PaymentResult confirm(String paymentKey, String orderId, long amount) {
        Order order = getOrder(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }

        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult result = confirmPayment(confirmation, orderId);

        paymentRepository.save(new Payment(result.paymentKey(), result.orderId()));
        orderRepository.updateStatus(orderId, OrderStatus.CONFIRMED);

        return result;
    }

    private PaymentResult confirmPayment(PaymentConfirmation confirmation, String orderId) {
        try {
            return paymentGateway.confirm(confirmation);
        } catch (PaymentConfirmationUnknownException exception) {
            orderRepository.updateStatus(orderId, OrderStatus.REQUIRES_CONFIRMATION);
            throw exception;
        } catch (TossPaymentException exception) {
            orderRepository.updateStatus(orderId, OrderStatus.FAILED);
            throw exception;
        }
    }

    private Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("해당 주문 정보를 찾을 수 없습니다."));
    }
}
