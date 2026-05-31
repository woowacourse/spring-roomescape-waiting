package roomescape.projection;

import java.time.LocalDateTime;
import roomescape.domain.Reservation;

public record ReservationWaitingWithOrder(
        Long id,
        String name,
        LocalDateTime createdAt,
        Reservation reservation,
        int order
) {
}
