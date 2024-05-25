package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaiting;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme()
                        .getName(),
                reservation.getDate(),
                reservation.getTime()
                        .getStartAt(),
                reservation.getStatus()
        );
    }

    public static MyReservationResponse from(ReservationWaiting reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus()
        );
    }
}
