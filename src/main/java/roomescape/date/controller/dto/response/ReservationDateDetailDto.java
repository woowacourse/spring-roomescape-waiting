package roomescape.date.controller.dto.response;

import java.time.LocalDate;
import roomescape.date.domain.ReservationDate;

public record ReservationDateDetailDto(
    Long id,
    LocalDate date,
    boolean isActive
) {

    public static ReservationDateDetailDto from(ReservationDate reservationDate) {
        return new ReservationDateDetailDto(reservationDate.getId(), reservationDate.getDate(),
            reservationDate.isActive());
    }
}
