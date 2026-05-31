package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.dao.dto.RankedWaiting;

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
    public static WaitingWithRankResponse from(RankedWaiting rankedWaiting) {
        return new WaitingWithRankResponse(
                rankedWaiting.id(),
                rankedWaiting.createdAt(),
                rankedWaiting.slotId(),
                rankedWaiting.name(),
                rankedWaiting.rank(),
                rankedWaiting.date(),
                rankedWaiting.startAt(),
                rankedWaiting.themeName());
    }
}
