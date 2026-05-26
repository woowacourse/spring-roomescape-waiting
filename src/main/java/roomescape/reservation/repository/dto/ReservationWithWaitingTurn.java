package roomescape.reservation.repository.dto;

import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationWithWaitingTurn(
        Long id,
        String name,
        LocalDate date,
        LocalTime startAt,
        Long themeId,
        String themeName,
        String themeThumbnailUrl,
        ReservationStatus status,
        LocalDateTime reservedAt,
        Long waitingTurn
) {
}
