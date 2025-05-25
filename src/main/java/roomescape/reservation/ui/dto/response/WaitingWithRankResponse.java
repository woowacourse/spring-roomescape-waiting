package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.MemberResponse.IdName;
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
            ReservationTimeResponse time,
            ThemeResponse theme,
            String status
    ) {
        public static WaitingWithRankResponse.ForMember from(final WaitingWithRankProjection projection) {
            return new WaitingWithRankResponse.ForMember(
                    projection.getId(),
                    projection.getDate(),
                    new ReservationTimeResponse(projection.getTimeId(), projection.getTimeStartAt()),
                    new ThemeResponse(
                            projection.getThemeId(),
                            projection.getThemeName(), projection.getThemeDescription(),
                            projection.getThemeThumbnail()
                    ),
                    projection.getRank() + "번째 예약 대기"
            );
        }
    }
}
