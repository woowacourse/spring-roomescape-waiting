package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record UpdateMyReservation(
    LocalDate date,
    Long timeId
) {

}