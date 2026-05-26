package roomescape.domain.reservationdate.admin.dto;

import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

public record CreateReservationDateResponse(
    Long id,
    LocalDate reservationDate
) {

    public static CreateReservationDateResponse from(ReservationDate reservationDate) {
        return new CreateReservationDateResponse(reservationDate.getId(), reservationDate.getDate());
    }
}
