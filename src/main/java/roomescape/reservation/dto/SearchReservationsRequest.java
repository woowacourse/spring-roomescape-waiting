package roomescape.reservation.dto;

import java.time.LocalDate;

public record SearchReservationsRequest(
        Long memberId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {
}
