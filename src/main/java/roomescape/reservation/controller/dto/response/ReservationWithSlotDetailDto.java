package roomescape.reservation.controller.dto.response;

import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWithSlotDetailDto(
        Long id,
        Long slotId,
        String name,
        LocalDate date,
        LocalTime time,
        Long themeId,
        String themeName,
        String themeThumbnailUrl,
        ReservationStatus status,
        Long waitingTurn
) {

    public static ReservationWithSlotDetailDto from(ReservationWithSlotInformation projection) {
        return new ReservationWithSlotDetailDto(
                projection.id(),
                projection.slotId(),
                projection.name(),
                projection.date(),
                projection.time(),
                projection.themeId(),
                projection.themeName(),
                projection.themeThumbnailUrl(),
                projection.status(),
                projection.waitingTurn()
        );
    }

}
