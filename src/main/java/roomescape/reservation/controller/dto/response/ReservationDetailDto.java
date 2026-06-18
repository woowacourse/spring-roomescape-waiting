package roomescape.reservation.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

public record ReservationDetailDto(
    Long id,
    Long memberId,
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
            reservation.getMember().getId(),
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
            reservation.memberId(),
            reservation.date(),
            reservation.startAt(),
            reservation.themeId(),
            reservation.themeName(),
            reservation.themeThumbnailUrl(),
            reservation.status(),
            reservation.waitingTurn()
        );
    }

}
