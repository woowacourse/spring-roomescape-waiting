package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Waiting;

public record WaitingReadResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String memberName,
        String themeName
) {
    public static WaitingReadResponse from(Waiting waiting) {
        return new WaitingReadResponse(
                waiting.getId(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getMember().getName(),
                waiting.getTheme().getName()
        );
    }
}
