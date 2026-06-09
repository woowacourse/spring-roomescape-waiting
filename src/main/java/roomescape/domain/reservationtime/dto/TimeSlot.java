package roomescape.domain.reservationtime.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record TimeSlot(
    LocalDate date,
    Long timeId,
    Long themeId
) {

    public static TimeSlot from (Reservation reservation) {
        return new TimeSlot(
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
    }
}
