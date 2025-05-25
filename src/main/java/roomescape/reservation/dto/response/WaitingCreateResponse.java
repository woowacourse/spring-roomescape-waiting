package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Waiting;

public record WaitingCreateResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static WaitingCreateResponse from(Waiting waiting) {
        return new WaitingCreateResponse(
                waiting.getId(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName()
        );
    }
}
