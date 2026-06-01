package roomescape.reservation.domain.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.reservation.domain.TimeSlot;

@Builder
public record ReservationQueryResult(
        Long id,
        String name,
        TimeSlot slot,
        Long pendingIndex,
        LocalDateTime createdAt
) {
}
