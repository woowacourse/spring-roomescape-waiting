package roomescape.domain.policy;

import static java.time.Month.DECEMBER;

import java.time.LocalDateTime;

public class FixeDueTimePolicy implements ReservationDueTimePolicy {
    @Override
    public LocalDateTime getDueTime() {
        return LocalDateTime.of(1998, DECEMBER, 11, 1, 0);
    }
}
