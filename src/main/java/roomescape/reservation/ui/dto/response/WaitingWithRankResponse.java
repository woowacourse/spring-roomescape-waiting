package roomescape.reservation.ui.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.MemberResponse.IdName;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.infrastructure.projection.WaitingWithRankProjection;
import roomescape.theme.ui.dto.ThemeResponse;

public record WaitingWithRankResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        IdName member,
        String status
) {

    public static WaitingWithRankResponse from(final WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResponse(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getReservationSlot().getDate(),
                ReservationTimeResponse.from(waitingWithRank.waiting().getReservationSlot().getTime()),
                ThemeResponse.from(waitingWithRank.waiting().getReservationSlot().getTheme()),
                MemberResponse.IdName.from(waitingWithRank.waiting().getMember()),
                waitingWithRank.rank() + "번째 예약 대기"
        );
    }

    public static WaitingWithRankResponse from(final WaitingWithRankProjection projection) {
        return new WaitingWithRankResponse(
                projection.getId(),
                projection.getDate(),
                new ReservationTimeResponse(projection.getTimeId(), projection.getTimeStartAt()),
                new ThemeResponse(
                        projection.getThemeId(),
                        projection.getThemeName(), projection.getThemeDescription(),
                        projection.getThemeThumbnail()
                ),
                new MemberResponse.IdName(projection.getMemberId(), projection.getMemberName()),
                projection.getRank() + "번째 예약 대기"
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
                    waitingWithRank.waiting().getId(),
                    waitingWithRank.waiting().getReservationSlot().getDate(),
                    waitingWithRank.waiting().getReservationSlot().getTime().getStartAt(),
                    waitingWithRank.waiting().getReservationSlot().getTheme().getName(),
                    waitingWithRank.rank() + "번째 예약 대기"
            );
        }
    }
}
