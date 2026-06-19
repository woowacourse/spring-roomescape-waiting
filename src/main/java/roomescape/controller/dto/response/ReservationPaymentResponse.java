package roomescape.controller.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;

public record ReservationPaymentResponse(
        long id,
        String orderId,
        long amount,
        String clientKey,
        String orderName,
        ReservationStatus reservationStatus
) {

    public static ReservationPaymentResponse from(ReservationPayment payment, String clientKey) {
        Reservation reservation = payment.getReservation();
        return new ReservationPaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                clientKey,
                reservation.getTheme().getName() + " 예약",
                ReservationStatus.PAYMENT_PENDING
        );
    }
}
