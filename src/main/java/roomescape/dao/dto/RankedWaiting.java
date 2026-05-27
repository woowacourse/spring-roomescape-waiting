package roomescape.dao.dto;

import java.time.LocalDateTime;

public record RankedWaiting(
        long id,
        LocalDateTime createdAt,
        long slotId,
        String name,
        int rank
) {
}
