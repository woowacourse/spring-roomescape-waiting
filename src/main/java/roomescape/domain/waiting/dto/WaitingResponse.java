package roomescape.domain.waiting.dto;

import java.time.LocalDate;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public static WaitingResponse of(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        );
    }
}
