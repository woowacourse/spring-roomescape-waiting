package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaitingWithRank;

public record ReservationWaitingWithRankDto(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeDto reservationTimeDto,
        ThemeDto themeDto,
        Long rank,
        String status) {

    public ReservationWaitingWithRankDto(ReservationWaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getMember().getName().getName(),
                waitingWithRank.getWaiting().getDate(),
                new ReservationTimeDto(
                        waitingWithRank.getWaiting().getTime().getId(),
                        waitingWithRank.getWaiting().getTime().getStartAt()),
                new ThemeDto(waitingWithRank.getWaiting().getTheme().getId(),
                        waitingWithRank.getWaiting().getTheme().getName(),
                        waitingWithRank.getWaiting().getTheme().getDescription(),
                        waitingWithRank.getWaiting().getTheme().getThumbnail()),
                waitingWithRank.getRank(),
                waitingWithRank.getWaiting().getStatus().name()
        );
    }
}
