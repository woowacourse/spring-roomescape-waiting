package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        ReservationStatus status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getStatus()
        );
    }
}
