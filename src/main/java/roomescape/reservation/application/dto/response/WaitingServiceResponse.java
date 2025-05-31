package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Waiting;

public record WaitingServiceResponse(
    Long id,
    String name,
    LocalDate date,
    LocalTime startAt,
    String themeName
) {

    public static WaitingServiceResponse from(Waiting waiting) {
        return new WaitingServiceResponse(
            waiting.getId(),
            waiting.getMember().getName(),
            waiting.getDate(),
            waiting.getTime().getStartAt(),
            waiting.getTheme().getName()
        );
    }
}

