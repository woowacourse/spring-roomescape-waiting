package roomescape.service.request;

import java.time.LocalDate;

public record ReservationWaitingAppRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
}
