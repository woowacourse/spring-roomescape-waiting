package roomescape.payment.web;

import roomescape.payment.Order;
import roomescape.reservation.Reservation;

/**
 * 예약 생성 응답. 프론트가 이 값으로 토스 결제창을 띄운다(orderId·amount·orderName).
 */
public record PaymentReadyResponse(Long reservationId, String orderId, long amount, String orderName) {
    public static PaymentReadyResponse from(Reservation reservation, Order order) {
        return new PaymentReadyResponse(
                reservation.getId(),
                order.getOrderId(),
                order.getAmount(),
                reservation.getTheme().getName().getValue()
        );
    }
}
