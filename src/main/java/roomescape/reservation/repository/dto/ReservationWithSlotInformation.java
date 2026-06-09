package roomescape.reservation.repository.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationWithSlotInformation(
        Long id,
        Long slotId,
        String name,
        LocalDate date,
        LocalTime time,
        Long themeId,
        String themeName,
        String themeThumbnailUrl,
        ReservationStatus status,
        LocalDateTime reservedAt,
        Long waitingTurn
) {

    public static ReservationWithSlotInformation from(Reservation reservation, ReservationSlot slot) {
        return new ReservationWithSlotInformation(
                reservation.getId(),
                slot.getId(),
                reservation.getName(),
                slot.getDate().getDate(),
                slot.getTime().getStartAt(),
                slot.getThemeId(),
                slot.getTheme().getName(),
                slot.getTheme().getThumbnailUrl(),
                reservation.getStatus(),
                reservation.getReservedAt(),
                null
        );
    }

}
