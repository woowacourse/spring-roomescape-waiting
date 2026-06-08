package roomescape.waiting.presentation.dto.response;

import java.time.LocalTime;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        String date,
        LocalTime time,
        String theme,
        Long rank
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate().toString(),
                waiting.getTime().getStartAt(),
                waiting.getTheme().getName(),
                waiting.getRank()
        );
    }
}
