package roomescape.reservation.ui.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
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

    public record ForMember(
            Long id,
            LocalDate date,
            @JsonFormat(pattern = "HH:mm")
            LocalTime time,
            String themeName,
            String status
    ) {

        public static WaitingWithRankResponse.ForMember from(final WaitingWithRank waitingWithRank) {
            return new WaitingWithRankResponse.ForMember(
                    waitingWithRank.getWaiting().getId(),
                    waitingWithRank.getWaiting().getDate(),
                    waitingWithRank.getWaiting().getTime().getStartAt(),
                    waitingWithRank.getWaiting().getTheme().getName(),
                    waitingWithRank.getRank() + "번째 예약 대기"
            );
        }
    }
}
