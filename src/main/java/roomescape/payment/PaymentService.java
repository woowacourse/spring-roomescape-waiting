package roomescape.payment;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.NotFoundException;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.PaymentTimedOutException;
import roomescape.payment.order.Order;
import roomescape.payment.order.OrderRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservation.exception.ReservationErrorMessage;

/**
 * 결제 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;

    public CheckoutInfo prepareCheckout(Long reservationId) {
        ReservationDetail detail = reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new NotFoundException(ReservationErrorMessage.RESERVATION_NOT_FOUND, reservationId));

        String orderId = UUID.randomUUID().toString();

        orderRepository.save(new Order(orderId, detail.amount(), reservationId));

        return new CheckoutInfo(orderId, detail.amount(), detail.themeName());
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        requireMatchingAmount(amount, order);

        try {
            var result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
            orderRepository.updatePaymentKey(orderId, result.paymentKey());
            reservationRepository.confirmPayment(order.getReservationId());
            return result;
        } catch (PaymentTimedOutException e) {
            handleTimeout(orderId);
            throw e;
        }
    }

    private void requireMatchingAmount(Long amount, Order order) {
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
    }

    private void handleTimeout(String orderId) {
        var order = orderRepository.getByOrderId(orderId);
        reservationRepository.markAsUncertain(order.getReservationId());
    }

    public String getReservationName(String orderId) {
        var order = orderRepository.getByOrderId(orderId);
        return reservationRepository.findById(order.getReservationId())
                .orElseThrow(() -> new IllegalStateException("결제 완료 후 예약을 찾을 수 없음: " + order.getReservationId()))
                .getName();
    }
}
