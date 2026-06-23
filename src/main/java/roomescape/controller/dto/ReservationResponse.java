package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public record ReservationResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTimeSlot()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus()
        );
    }
}
