package roomescape.controller.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MyReservationResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status) {

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getValue()
        );
    }

    public static MyReservationResponse from(final Reservation reservation, final String rank) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                rank + " " + reservation.getStatus().getValue()
        );
    }
}
