package roomescape.service;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationSlot(
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public static ReservationSlot from(Reservation reservation) {
        return new ReservationSlot(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        );
    }
}
