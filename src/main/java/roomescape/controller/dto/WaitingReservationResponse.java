package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record WaitingReservationResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        String status,
        int waitingOrder
) {
    public static WaitingReservationResponse from(int order, Reservation reservation) {
        return new WaitingReservationResponse(reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getReservationStatusName(),
                order);
    }
}
