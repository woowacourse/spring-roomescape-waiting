package roomescape.payment.web;

import roomescape.order.Order;
import roomescape.reservation.Reservation;

/**
 * 결제 준비 응답. 결제 시작 시점에 만든 주문 정보로 프론트가 토스 결제창을 띄운다(orderId·amount·orderName).
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
