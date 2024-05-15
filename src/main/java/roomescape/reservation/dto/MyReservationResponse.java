package roomescape.reservation.dto;

import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate().getValue(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getDescription()
        );
    }
}
