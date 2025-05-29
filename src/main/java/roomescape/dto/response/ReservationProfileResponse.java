package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record ReservationProfileResponse(
        long id,
        LocalDate date,
        String themeName,
        LocalTime startAt
) {

    public ReservationProfileResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getDate(), reservation.getTheme().getName(),
                reservation.getReservationTime().getStartAt());
    }
}
