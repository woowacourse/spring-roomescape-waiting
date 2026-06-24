package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.payment.ReservationPayment;
import roomescape.domain.reservation.Reservation;

public record MyReservationResponse(
    Long id,
    String name,
    LocalDate date,
    LocalTime time,
    String themeName,
    PaymentStatus paymentStatus,
    String orderId,
    String paymentKey,
    Long amount
) {

    public static MyReservationResponse from(Reservation reservation, ReservationPayment payment) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            reservation.getTheme().getName(),
            payment == null ? PaymentStatus.PENDING : payment.status(),
            payment == null ? null : payment.orderId(),
            payment == null ? null : payment.paymentKey(),
            payment == null ? null : payment.amount()
        );
    }
}
