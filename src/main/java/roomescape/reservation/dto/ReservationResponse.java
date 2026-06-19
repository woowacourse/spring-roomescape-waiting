package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.payment.domain.Payment;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.TimeResponse;

public record ReservationResponse(
        Long id,
        String memberName,
        LocalDate date,
        TimeResponse time,
        Long themeId,
        String themeName,
        String status,
        Long price,
        String orderId,
        String paymentStatus,
        String paymentKey,
        Long paymentAmount
) {

    public static ReservationResponse from(Reservation reservation) {
        return of(reservation, null);
    }

    public static ReservationResponse of(Reservation reservation, Payment payment) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                TimeResponse.of(reservation.getTime()),
                reservation.getTheme().getId(),
                reservation.getTheme().getName(),
                reservation.getStatus().name(),
                reservation.getTheme().getPrice(),
                payment == null ? null : payment.getOrderId(),
                payment == null ? null : payment.getState().name(),
                payment == null ? null : payment.getPaymentKey(),
                payment == null ? null : payment.getAmount()
        );
    }
}