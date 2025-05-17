package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;
import roomescape.theme.entity.Theme;

public record ReservationCreateResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static ReservationCreateResponse from(Reservation reservation, Theme theme) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                theme.getName()
        );
    }
}
