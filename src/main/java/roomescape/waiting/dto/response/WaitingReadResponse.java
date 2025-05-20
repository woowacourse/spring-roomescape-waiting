package roomescape.waiting.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.entity.Waiting;

public record WaitingReadResponse(
        Long id,
        String name,
        LocalDate date,
        String theme,
        LocalTime time
) {
    public static WaitingReadResponse from(Waiting waiting) {
        return new WaitingReadResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                waiting.getTheme().getName(),
                waiting.getTime().getStartAt()
        );
    }
}
