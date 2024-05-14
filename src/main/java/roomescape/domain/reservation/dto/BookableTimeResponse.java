package roomescape.domain.reservation.dto;

import java.time.LocalTime;

public record BookableTimeResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

}
