package roomescape.dto;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long memberId, long timeId, long themeId) {
}
