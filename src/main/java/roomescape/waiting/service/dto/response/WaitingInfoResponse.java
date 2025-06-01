package roomescape.waiting.service.dto.response;

import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record WaitingInfoResponse(
        Long id,
        String member,
        String theme,
        LocalDate date,
        LocalTime time,
        LocalDateTime createdAt
) {
    public static WaitingInfoResponse from(Waiting waiting) {
        return new WaitingInfoResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getCreatedAt()
        );
    }
}
