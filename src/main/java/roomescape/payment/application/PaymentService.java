package roomescape.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.dto.PaymentConfirmRequest;
import roomescape.payment.dto.PaymentConfirmResponse;
import roomescape.payment.exception.PaymentErrorCode;
import roomescape.payment.repository.OrderRepository;
import roomescape.reservation.service.ReservationService;

@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final ReservationService reservationService;

    public PaymentService(PaymentGateway paymentGateway, OrderRepository orderRepository,
                          ReservationService reservationService) {
        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));
        order.verifyAmount(request.amount());

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(order.getOrderId().value(), request.paymentKey(), request.amount()));

        Order confirmed = orderRepository.updatePayment(order.confirm(result.paymentKey()));
        reservationService.confirmReservation(confirmed.getReservationId());
        return PaymentConfirmResponse.from(confirmed);
    }

    @Transactional
    public void fail(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        orderRepository.findByOrderId(orderId)
                .ifPresent(order -> reservationService.cancel(order.getReservationId()));
    }
}
