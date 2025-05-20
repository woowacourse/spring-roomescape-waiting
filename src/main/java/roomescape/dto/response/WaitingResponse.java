package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Waiting;

public record WaitingResponse (
        Long id,
        String name,
        LocalTime time,
        LocalDate date,
        String themeName
){
    public static WaitingResponse from(Waiting waiting) {

        return new WaitingResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getStartAt(),
                waiting.getDate(),
                waiting.getThemeName()
        );
    }
}
