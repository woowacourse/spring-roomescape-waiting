package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.payment.domain.Payment;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.TimeResponse;

public record ReservationCreateResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        Long themeId,
        String themeName,
        PaymentStatus status,
        String orderId,
        Long amount
) {

    public static ReservationCreateResponse of(Reservation reservation, Payment payment) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTime()),
                reservation.getTheme().getId(),
                reservation.getTheme().getName(),
                reservation.getStatus(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }
}
