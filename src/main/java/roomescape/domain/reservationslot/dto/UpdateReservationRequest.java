package roomescape.domain.reservationslot.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateReservationRequest(
    LocalDate startWhen,
    LocalTime startAt
) {

}
