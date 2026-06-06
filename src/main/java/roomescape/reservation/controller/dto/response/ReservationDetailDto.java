package roomescape.reservation.controller.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDetailDto(
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

    public static ReservationDetailDto from(Reservation reservation) {
        return new ReservationDetailDto(
                reservation.getId(),
                null,
                reservation.getName(),
                reservation.getDate().getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getId(),
                reservation.getTheme().getName(),
                reservation.getTheme().getThumbnailUrl(),
                reservation.getStatus(),
                null
        );
    }

    public static ReservationDetailDto from(ReservationWithWaitingTurn reservation) {
        return new ReservationDetailDto(
                reservation.id(),
                null,
                reservation.name(),
                reservation.date(),
                reservation.startAt(),
                reservation.themeId(),
                reservation.themeName(),
                reservation.themeThumbnailUrl(),
                reservation.status(),
                reservation.waitingTurn()
        );
    }

    public static ReservationDetailDto from(ReservationWithSlotInformation projection) {
        return new ReservationDetailDto(
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
