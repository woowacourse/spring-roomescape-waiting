package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.command.ReservationCommandService;
import roomescape.application.payment.PaymentGateway;
import roomescape.application.payment.exception.PaymentAmountMismatchException;
import roomescape.application.payment.model.PaymentConfirmation;
import roomescape.application.payment.model.PaymentResult;
import roomescape.domain.Order;
import roomescape.domain.OrderRepository;

/**
 * 결제 승인 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
public class ReservationPaymentUseCase {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationCommandService reservationCommandService;

    public ReservationPaymentUseCase(
            OrderRepository orderRepository,
            PaymentGateway paymentGateway,
            ReservationCommandService reservationCommandService
    ) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.reservationCommandService = reservationCommandService;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }

        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult result = paymentGateway.confirm(confirmation);

        reservationCommandService.save(order.getReservation());
        orderRepository.save(order.confirm(result.paymentKey()));

        return result;
    }

}
