package roomescape.dto;

import java.time.LocalDate;

public record ReservationRequest(Long memberId, LocalDate date, long timeId, long themeId) {
}
