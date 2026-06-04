package roomescape.reservation.presentation.response;

import java.time.LocalDate;
import roomescape.reservation.domain.ReservationDate;

public record ReservationDateResponse(
    Long id,
    LocalDate reservationDate
) {

    public static ReservationDateResponse from(ReservationDate reservationDate) {
        return new ReservationDateResponse(
            reservationDate.getId(),
            reservationDate.getDate()
        );
    }
}
