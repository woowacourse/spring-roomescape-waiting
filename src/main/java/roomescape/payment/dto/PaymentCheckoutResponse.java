package roomescape.payment.dto;

import roomescape.payment.PaymentCheckout;
import roomescape.reservation.ReservationStatus;

public record PaymentCheckoutResponse(
        Long id,
        Long memberId,
        Long reservationId,
        ReservationStatus reservationStatus,
        String orderId,
        String orderName,
        Long amount,
        String clientKey
) {
    public static PaymentCheckoutResponse from(PaymentCheckout checkout, String clientKey) {
        return new PaymentCheckoutResponse(
                checkout.reservation().getId(),
                checkout.reservation().getMemberId(),
                checkout.reservation().getId(),
                checkout.reservation().getStatus(),
                checkout.orderId(),
                checkout.orderName(),
                checkout.amount(),
                clientKey
        );
    }
}
