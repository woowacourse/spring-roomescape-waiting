package roomescape.dto;

import java.time.LocalDate;

import roomescape.domain.Reservation;

public record ReservationWaitingRequest(
        LocalDate date,
        Long timeId,
        Long themeId
) implements ReservationRequest {

    public static ReservationWaitingRequest from(Reservation reservation) {
        return new ReservationWaitingRequest(
                reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId()
        );
    }
}
