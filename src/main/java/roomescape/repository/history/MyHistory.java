package roomescape.repository.history;

import java.time.LocalDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record MyHistory(
        String status,
        String name,
        LocalDate date,
        Theme theme,
        ReservationTime time,
        Integer sequence
) {
}
