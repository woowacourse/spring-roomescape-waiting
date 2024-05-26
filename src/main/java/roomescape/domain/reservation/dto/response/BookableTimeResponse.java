package roomescape.domain.reservation.dto.response;

import java.time.LocalTime;

public record BookableTimeResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

}
