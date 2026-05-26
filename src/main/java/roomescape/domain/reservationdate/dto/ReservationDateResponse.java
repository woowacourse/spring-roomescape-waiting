package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

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
