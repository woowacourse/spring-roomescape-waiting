package roomescape.dao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record RankedWaiting(
        long id,
        LocalDateTime createdAt,
        long slotId,
        String name,
        int rank,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
}
