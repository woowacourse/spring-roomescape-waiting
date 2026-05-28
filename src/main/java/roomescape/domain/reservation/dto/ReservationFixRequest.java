package roomescape.domain.reservation.dto;

import java.time.LocalDate;

public record ReservationFixRequest(
    String name,
    LocalDate date,
    Long timeId
) {

}
