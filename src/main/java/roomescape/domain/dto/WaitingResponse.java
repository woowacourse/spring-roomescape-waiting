package roomescape.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Waiting;

public record WaitingResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
    public static WaitingResponse from(final Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getStatus().getMessage());
    }
}
