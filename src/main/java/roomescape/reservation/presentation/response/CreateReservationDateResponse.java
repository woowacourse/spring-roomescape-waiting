package roomescape.reservation.presentation.response;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;

public record CreateReservationDateResponse(
    Long id,
    LocalDate reservationDate
) {

    public static CreateReservationDateResponse from(ReservationDate reservationDate) {
        return new CreateReservationDateResponse(reservationDate.getId(), reservationDate.getDate());
    }
}
