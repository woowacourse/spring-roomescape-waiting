package roomescape.waiting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingWithRankResponse(
        Long id,
        String name,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public static WaitingWithRankResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new WaitingWithRankResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt()
        );
    }
}
