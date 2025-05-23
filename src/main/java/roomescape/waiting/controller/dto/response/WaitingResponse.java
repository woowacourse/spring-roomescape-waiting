package roomescape.waiting.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.waiting.entity.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(
        Long waitingId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt(),
                waiting.getStatusText()
        );
    }
}
