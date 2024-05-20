package roomescape.reservation.dto;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId) {
}
