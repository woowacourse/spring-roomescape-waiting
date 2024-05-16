package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(final ReservationDto reservation) {
        return new MyReservationResponse(
                reservation.id(),
                reservation.theme().name().getValue(),
                reservation.date().getValue(),
                reservation.time().startAt(),
                reservation.status().getDescription()
        );
    }
}
