package roomescape.reservation.dto;

import java.time.LocalDate;

public record SearchReservationsParams(
        Long memberId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {
}
