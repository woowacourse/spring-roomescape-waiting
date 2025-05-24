package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.theme.ui.dto.ThemeResponse;

public record WaitingWithRankResponse(
        Long id,
        IdName member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static WaitingWithRankResponse from(final WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResponse(
                waitingWithRank.getWaiting().getId(),
                MemberResponse.IdName.from(waitingWithRank.getWaiting().getMember()),
                waitingWithRank.getWaiting().getDate(),
                ReservationTimeResponse.from(waitingWithRank.getWaiting().getTime()),
                ThemeResponse.from(waitingWithRank.getWaiting().getTheme()),
                waitingWithRank.getRank() + "번째 예약 대기"
        );
    }
}
