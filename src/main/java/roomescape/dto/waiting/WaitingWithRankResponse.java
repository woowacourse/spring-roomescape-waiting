package roomescape.dto.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.WaitingWithRank;

public record WaitingWithRankResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {

    public static WaitingWithRankResponse from(WaitingWithRank waiting) {
        return new WaitingWithRankResponse(waiting.getWaiting().getId(), waiting.getWaiting().getMember().
                getName(), waiting.getWaiting().getTheme().getName(), waiting.getWaiting().getDate(),
                waiting.getWaiting().getTime().getStartAt());
    }

}
