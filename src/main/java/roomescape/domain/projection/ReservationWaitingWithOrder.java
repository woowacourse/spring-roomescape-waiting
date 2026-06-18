package roomescape.domain.projection;

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

    // JPA 3단계
    public ReservationWaitingWithOrder(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Long order
    ) {
        this(id, name, date, time, theme, Math.toIntExact(order));
    }
}
