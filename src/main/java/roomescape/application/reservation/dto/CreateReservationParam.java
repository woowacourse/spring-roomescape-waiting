package roomescape.application.reservation.dto;

import java.time.LocalDate;

public record CreateReservationParam(
        LocalDate date,
        Long timeId,
        Long themeId,
        Long memberId
) {
}
