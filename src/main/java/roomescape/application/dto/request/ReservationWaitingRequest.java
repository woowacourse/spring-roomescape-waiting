package roomescape.application.dto.request;

import java.time.LocalDate;

public record ReservationWaitingRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
}
