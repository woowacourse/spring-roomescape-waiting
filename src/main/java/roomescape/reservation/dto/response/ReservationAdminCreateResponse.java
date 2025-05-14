package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;
import roomescape.theme.entity.Theme;

public record ReservationAdminCreateResponse(
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static ReservationAdminCreateResponse from(Reservation reservation, Theme theme) {
        return new ReservationAdminCreateResponse(
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                theme.getName()
        );
    }
}
