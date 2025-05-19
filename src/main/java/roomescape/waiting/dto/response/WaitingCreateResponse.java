package roomescape.waiting.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.entity.Waiting;

public record WaitingCreateResponse(
        Long waitingId,
        LocalDate date,
        String theme,
        LocalTime time,
        Long rank
) {
    public static WaitingCreateResponse from(Waiting waiting, Long rank) {
        return new WaitingCreateResponse(
                waiting.getId(),
                waiting.getDate(),
                waiting.getTheme().getName(),
                waiting.getTime().getStartAt(),
                rank
        );
    }
}
