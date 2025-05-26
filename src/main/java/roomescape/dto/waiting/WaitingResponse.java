package roomescape.dto.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        String theme,
        LocalTime startAt) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getMember().getName(), waiting.getDate(),
                waiting.getTheme().getName(),
                waiting.getTime().getStartAt());
    }
}
