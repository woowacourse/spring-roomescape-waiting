package roomescape.domain.policy;

import java.time.LocalDateTime;

public class CurrentDueTimePolicy implements ReservationDueTimePolicy {
    @Override
    public LocalDateTime getDueTime() {
        return LocalDateTime.now();
    }
}
