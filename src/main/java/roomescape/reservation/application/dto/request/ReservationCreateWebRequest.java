package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public record ReservationCreateWebRequest(LocalDate date, Long timeId, Long themeId) {
}
