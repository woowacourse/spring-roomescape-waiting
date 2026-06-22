package roomescape.controller.dto;

import roomescape.domain.Reservation;
import roomescape.domain.payment.Payment;

import java.time.LocalDate;

public record ReservationPaymentResponse(
        Long reservationId,
        String themeName,
        LocalDate date,
        String timeValue,
        String reservationStatus,
        String orderId,
        String paymentKey,
        Long amount,
        String paymentStatus
) {
    public static ReservationPaymentResponse of(Reservation reservation, Payment payment) {
        return new ReservationPaymentResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt().toString(),
                reservation.getReservationStatusName(),
                reservation.getOrderId(),
                payment.getPaymentKey(),
                payment.getAmount(),
                payment.getStatus().name()
        );
    }

    public static ReservationPaymentResponse withoutPayment(Reservation reservation) {
        return new ReservationPaymentResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt().toString(),
                reservation.getReservationStatusName(),
                reservation.getOrderId(),
                null,
                reservation.getAmount(),
                null
        );
    }
}
