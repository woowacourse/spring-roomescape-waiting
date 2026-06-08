package roomescape.domain.reservation.vo;

import java.time.LocalDate;

public record ReservationSchedule(LocalDate date, Long themeId, Long timeId) {
}
