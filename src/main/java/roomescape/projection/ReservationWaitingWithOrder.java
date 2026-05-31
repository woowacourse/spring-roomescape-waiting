package roomescape.projection;

import java.time.LocalDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationWaitingWithOrder(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        int order
) {
}
