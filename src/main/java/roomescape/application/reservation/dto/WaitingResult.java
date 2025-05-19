package roomescape.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Waiting;

public record WaitingResult(
        Long waitingId,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Long waitingCount
) {

    public static WaitingResult from(Waiting waiting, Long waitingCount) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waitingCount
        );
    }
}
