package roomescape.application.reservation.dto;

import java.time.LocalDate;

public record CreateWaitingParam(
        LocalDate reservationDate,
        Long themeId,
        Long timeId,
        Long memberId
) {
}
