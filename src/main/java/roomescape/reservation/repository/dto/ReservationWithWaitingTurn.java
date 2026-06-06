package roomescape.reservation.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationWithWaitingTurn(
    Long id,
    String name,
    LocalDate date,
    LocalTime startAt,
    Long themeId,
    String themeName,
    String themeThumbnailUrl,
    ReservationStatus status,
    Long waitingTurn
) {

}
