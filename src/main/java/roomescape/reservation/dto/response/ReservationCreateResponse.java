package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

public record ReservationCreateResponse(
        Long id,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
    public static ReservationCreateResponse from(Reservation reservation, Theme theme) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime(),
                theme
        );
    }
}
