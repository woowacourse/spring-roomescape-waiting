package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public record CreateWaitingServiceRequest (
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
    ) {
}
