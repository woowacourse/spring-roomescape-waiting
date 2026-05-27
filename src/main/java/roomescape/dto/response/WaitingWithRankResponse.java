package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record WaitingWithRankResponse(
        long id,
        LocalDateTime createdAt,
        long slotId,
        String name,
        int rank,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String themeName
) {
}
