package roomescape.controller.dto.response;

import roomescape.domain.Payment;

public record PaymentReadyResponse(
        Long reservationId,
        Long paymentId,
        String orderId,
        Long amount
) {

    public static PaymentReadyResponse from(Payment payment) {
        return new PaymentReadyResponse(
                payment.getReservationId(),
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }
}
