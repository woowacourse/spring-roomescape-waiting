package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

public record ReservationAdminCreateResponse(
        LocalDate date,
        ReservationTime time,
        Theme theme
) {

    public static ReservationAdminCreateResponse from(Reservation reservation, Theme theme) {
        return new ReservationAdminCreateResponse(
                reservation.getDate(),
                reservation.getTime(),
                theme
        );
    }

}
