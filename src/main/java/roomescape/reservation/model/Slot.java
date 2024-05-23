package roomescape.reservation.model;

import java.time.LocalDate;

public record Slot(
        LocalDate date,
        ReservationTime reservationTime,
        Theme theme
) {
}
