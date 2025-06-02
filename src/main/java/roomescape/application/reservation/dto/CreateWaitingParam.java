package roomescape.application.reservation.dto;

import java.time.LocalDate;

public record CreateWaitingParam(
        LocalDate date,
        Long memberId,
        Long timeId,
        Long themeId
) {
}
