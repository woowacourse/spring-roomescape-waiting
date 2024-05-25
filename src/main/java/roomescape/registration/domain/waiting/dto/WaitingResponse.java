package roomescape.registration.domain.waiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.registration.domain.waiting.domain.Waiting;

public record WaitingResponse(
        long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime startAt
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt()
        );
    }
}
