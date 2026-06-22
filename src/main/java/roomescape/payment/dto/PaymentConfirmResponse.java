package roomescape.payment.dto;

import roomescape.payment.domain.Order;

public record PaymentConfirmResponse(
        Long reservationId,
        String orderId,
        String paymentKey,
        long amount,
        String status
) {

    public static PaymentConfirmResponse from(Order order) {
        return new PaymentConfirmResponse(
                order.getReservationId(),
                order.getOrderId().value(),
                order.getPaymentKey(),
                order.getAmount(),
                order.getStatus().name()
        );
    }
}
