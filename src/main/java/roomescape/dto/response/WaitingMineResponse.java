package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingMineResponse(long waitingId,
                                  String theme,
                                  LocalDate date,
                                  @JsonFormat(pattern = "HH:mm") LocalTime time,
                                  String status) {
    public static WaitingMineResponse from(Waiting waiting) {
        return new WaitingMineResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getStatus().name()
        );
    }
}
