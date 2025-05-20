package roomescape.reservation.dto;

import java.time.LocalDate;

public record AvailableReservationTimeRequest(
        LocalDate date,
        Long themeId
) {
}
