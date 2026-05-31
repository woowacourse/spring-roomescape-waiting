package roomescape.controller.dto;

import java.time.LocalDate;

public record ReservationPatchRequest(String name, LocalDate date, Long timeId, Long themeId) {
}
