package roomescape.domain.reservation.dto.request;

import java.time.LocalDate;

public record ReservationAddRequest(LocalDate date, Long timeId, Long themeId) {
}
