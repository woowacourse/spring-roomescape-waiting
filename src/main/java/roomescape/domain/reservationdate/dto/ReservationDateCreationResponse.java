package roomescape.domain.reservationdate.dto;

import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

public record ReservationDateCreationResponse(
    Long id,
    LocalDate playDay
) {

    public static ReservationDateCreationResponse from(ReservationDate reservationDate) {
        return new ReservationDateCreationResponse(reservationDate.getId(), reservationDate.getPlayDay());
    }
}
