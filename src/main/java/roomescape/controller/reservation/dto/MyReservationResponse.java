package roomescape.controller.reservation.dto;

import roomescape.domain.Reservation;
import roomescape.domain.Status;

import java.time.LocalDate;
import java.time.LocalTime;

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
                Status.RESERVED.getValue()
        );
    }
}
