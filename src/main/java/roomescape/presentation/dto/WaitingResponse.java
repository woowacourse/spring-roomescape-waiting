package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.business.domain.Waiting;

public record WaitingResponse(Long id, Long timeId, Long themeId, LocalDate date, LocalDateTime createdAt) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getReservationTime().getId(), waiting.getTheme().getId(),
                waiting.getReservationDate(), waiting.getCreatedAt());
    }
}
