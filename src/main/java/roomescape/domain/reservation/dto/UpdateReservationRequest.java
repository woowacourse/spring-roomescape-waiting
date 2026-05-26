package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateReservationRequest(
    LocalDate startWhen,
    LocalTime startAt
) {

}
