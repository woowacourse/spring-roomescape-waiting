package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.dto.business.WaitingWithRank;

public record WaitingWithRankResponse(
        long id,
        LocalDate date,
        String themeName,
        LocalTime startAt,
        long rank
) {

    public WaitingWithRankResponse(WaitingWithRank waitingWithRank) {
        this(waitingWithRank.getId(), waitingWithRank.getDate(), waitingWithRank.getThemeName(),
                waitingWithRank.getStartAt(), waitingWithRank.getRank());
    }
}
