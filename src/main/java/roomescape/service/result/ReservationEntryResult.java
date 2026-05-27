package roomescape.service.result;

import java.time.LocalDateTime;
import roomescape.domain.ReservationEntry;

public record ReservationEntryResult(
        long id,
        String name,
        String status,
        LocalDateTime createdAt
) {

    public static ReservationEntryResult from(ReservationEntry entry) {
        return new ReservationEntryResult(
                entry.getId(),
                entry.getName(),
                entry.getStatus().name(),
                entry.getCreatedAt()
        );
    }
}
