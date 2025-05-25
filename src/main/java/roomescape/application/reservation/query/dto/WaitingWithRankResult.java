package roomescape.application.reservation.query.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Waiting;

public record WaitingWithRankResult(
        Long waitingId,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Long waitingCount
) {

    public static WaitingWithRankResult from(Waiting waiting, Long waitingCount) {
        return new WaitingWithRankResult(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waitingCount
        );
    }
}
