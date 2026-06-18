package roomescape.service.dto.result;

import roomescape.domain.Payment;

public record PaymentReadyResult(
        Long id,
        String orderId,
        Long reservationId,
        Long amount
) {

    public static PaymentReadyResult from(final Payment payment) {
        return new PaymentReadyResult(
                payment.getId(),
                payment.getOrderId(),
                payment.getReservationId(),
                payment.getAmount()
        );
    }
}
