package roomescape.controller.dto.response;

import roomescape.domain.Payment;

public record PaymentCheckoutResponse(
        Long reservationId,
        Long paymentId,
        String checkoutUrl
) {
    public static PaymentCheckoutResponse from(Payment payment) {
        return new PaymentCheckoutResponse(
                payment.getReservationId(),
                payment.getId(),
                "/payments/" + payment.getId() + "/checkout"
        );
    }
}
