package roomescape.waiting.dto.response;

import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record AdminWaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime time
) {
    public static AdminWaitingResponse from(Waiting waiting) {
        return new AdminWaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getSchedule().getTheme().getName(),
                waiting.getSchedule().getDate(),
                waiting.getSchedule().getTime().getStartAt()
        );
    }
}
