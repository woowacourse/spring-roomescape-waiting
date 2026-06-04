package roomescape.reservation.presentation.response;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;

public record AdminReservationDateResponse(
    Long id,
    LocalDate reservationDate
) {

    public static AdminReservationDateResponse from(ReservationDate reservationDate) {
        return new AdminReservationDateResponse(reservationDate.getId(), reservationDate.getDate());
    }
}
