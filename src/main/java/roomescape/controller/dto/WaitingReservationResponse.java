package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

public record WaitingReservationResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        String status,
        int waitingOrder
) {
    public static WaitingReservationResponse from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        int waitingOrder = waitingWithRank.rank().intValue();
        return new WaitingReservationResponse(
                waiting.getId(),
                waiting.getMemberName(),
                waiting.getDate(),
                TimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                waitingOrder + "번째 예약대기",
                waitingOrder
        );
    }

}
