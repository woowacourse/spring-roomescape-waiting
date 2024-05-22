package roomescape.registration.reservation.dto;

import java.time.LocalDate;

public record ReservationRequest(
        LocalDate date,
        long timeId,
        long themeId
) {
}
