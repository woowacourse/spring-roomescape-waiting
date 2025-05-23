package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.dto.business.WaitingWithRank;

public record WaitingWithRankResponse(
        Long id,
        LocalDate date,
        ThemeProfileResponse theme,
        ReservationTimeResponse time,
        MemberProfileResponse member,
        Long rank
) {

    public WaitingWithRankResponse(WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.id(),
                waitingWithRank.date(),
                new ThemeProfileResponse(waitingWithRank.theme()),
                new ReservationTimeResponse(waitingWithRank.time()),
                new MemberProfileResponse(waitingWithRank.member()),
                waitingWithRank.rank()
        );
    }
}
