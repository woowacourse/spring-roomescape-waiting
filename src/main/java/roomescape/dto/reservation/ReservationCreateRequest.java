package roomescape.dto.reservation;

import java.time.LocalDate;

public record ReservationCreateRequest(LocalDate date, long themeId, long timeId, long memberId) {
}
