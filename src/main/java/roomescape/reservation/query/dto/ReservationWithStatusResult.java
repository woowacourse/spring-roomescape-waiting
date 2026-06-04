package roomescape.reservation.query.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;

public record ReservationWithStatusResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        String status,
        Long waitingOrder
) {

    public static ReservationWithStatusResult from(Reservation reservation) {
        return new ReservationWithStatusResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                "reserved",
                0L
        );
    }

    public static ReservationWithStatusResult from(ReservationWaiting waiting, long rank) {
        return new ReservationWithStatusResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                "waiting",
                rank
        );
    }
}
