package roomescape.reservation.dto;

import java.time.LocalDate;

public record AdminFilterReservationRequest(
        Long memberId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {
}
