package roomescape.reservation.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Builder
public record ReservationQueryResult(
        Long id,
        String name,
        LocalDate date,
        Status status,
        Long pendingIndex,
        LocalDateTime createdAt,
        Theme theme,
        ReservationTime time
) {
}
