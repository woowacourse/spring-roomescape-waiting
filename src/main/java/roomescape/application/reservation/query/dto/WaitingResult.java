package roomescape.application.reservation.query.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Waiting;

public record WaitingResult(
        Long waitingId,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime
) {

    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt()
        );
    }
}
