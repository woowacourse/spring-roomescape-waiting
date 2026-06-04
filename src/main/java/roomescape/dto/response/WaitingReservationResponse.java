package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record WaitingReservationResponse(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime time,
        int waitingOrder
) {
    public static WaitingReservationResponse from(Reservation reservation, int waitingOrder) {
        return new WaitingReservationResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                waitingOrder
        );
    }
}

