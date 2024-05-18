package roomescape.application.dto.request;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
