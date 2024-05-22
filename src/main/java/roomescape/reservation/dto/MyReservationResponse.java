package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        Integer order
) {

    public static MyReservationResponse toResponse(Reservation reservation, int order) {
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt(), reservation.getStatus().getStatus(), order);
    }
}
