package roomescape.dto.reservation;

import java.time.LocalDate;

public record ReservationCreateRequest(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
