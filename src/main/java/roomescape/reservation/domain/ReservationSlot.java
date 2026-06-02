package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationSlot(
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
}
