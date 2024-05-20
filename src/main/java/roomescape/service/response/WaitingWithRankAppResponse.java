package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.WaitingWithRank;

public record WaitingWithRankAppResponse(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse,
        Long rank) {

    public WaitingWithRankAppResponse(WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getMember().getName().getName(),
                waitingWithRank.getWaiting().getDate(),
                new ReservationTimeAppResponse(
                        waitingWithRank.getWaiting().getTime().getId(),
                        waitingWithRank.getWaiting().getTime().getStartAt()),
                new ThemeAppResponse(waitingWithRank.getWaiting().getTheme().getId(),
                        waitingWithRank.getWaiting().getTheme().getName(),
                        waitingWithRank.getWaiting().getTheme().getDescription(),
                        waitingWithRank.getWaiting().getTheme().getThumbnail()),
                waitingWithRank.getRank()
        );
    }
}
