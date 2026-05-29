package roomescape.history;

import java.time.LocalDate;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public record MyHistory(
        Long reservationId,
        Long waitingId,
        String status,
        String name,
        LocalDate date,
        Theme theme,
        ReservationTime time,
        Integer sequence
) {
}
