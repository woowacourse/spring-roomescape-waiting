package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.TimeResponse;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        Long themeId,
        String themeName,
        PaymentStatus status
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTime()),
                reservation.getTheme().getId(),
                reservation.getTheme().getName(),
                reservation.getStatus()
        );
    }
}
