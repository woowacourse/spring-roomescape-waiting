package roomescape.presentation.dto;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long memberId, Long timeId, Long themeId) {
}
