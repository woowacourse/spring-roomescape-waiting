package roomescape.presentation.api.reservation.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.application.reservation.dto.WaitingResult;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalDateTime startAt
) {

    public static WaitingResponse from(WaitingResult waitingResult) {
        return new WaitingResponse(
                waitingResult.waitingId(),
                waitingResult.memberName(),
                waitingResult.themeName(),
                waitingResult.date(),
                waitingResult.startedAt()
        );
    }
}
