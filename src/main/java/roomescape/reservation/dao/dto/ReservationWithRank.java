package roomescape.reservation.dao.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationWithRank(
        Long id,
        String name,
        LocalDate date,
        Long timeId,
        LocalTime startAt,
        Long themeId,
        String themeName,
        ReservationStatus status,
        Long waitRank
) {
}
