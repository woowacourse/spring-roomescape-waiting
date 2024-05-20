package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public MyReservationResponse(Reservation reservation) {
        this(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt(), "예약");
    }
}
