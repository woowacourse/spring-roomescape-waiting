package roomescape.domain.policy;

import java.time.LocalDateTime;

public interface ReservationDueTimePolicy {
    LocalDateTime getDueTime();
}
