package roomescape.domain.reservationslot.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateReservationSlotRequest(
    LocalDate startWhen,
    LocalTime startAt
) {

}
