package roomescape.payment.application.dto.response;

import roomescape.reservation.domain.ReservationStatus;

public record PaymentConfirmResponse(
        Long reservationId,
        String orderId,
        String paymentKey,
        int amount,
        ReservationStatus status
) {
}
