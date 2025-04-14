package roomescape.user.controller.dto;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long themeId, Long timeId) {
}
