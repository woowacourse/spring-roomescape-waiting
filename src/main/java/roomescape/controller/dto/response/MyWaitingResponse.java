package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.entity.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyWaitingResponse (
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status
) {

    public static MyWaitingResponse from(Waiting waiting) {
        return new MyWaitingResponse(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt(),
                waiting.getStatusText()
        );
    }
}
