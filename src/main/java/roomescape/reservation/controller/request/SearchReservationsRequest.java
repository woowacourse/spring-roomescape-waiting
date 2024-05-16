package roomescape.reservation.controller.request;

import java.time.LocalDate;

public record SearchReservationsRequest(
        Long memberId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {
}
