package roomescape.domain.dto;

import java.time.LocalTime;

public record BookResponse(LocalTime startAt, Long timeId, Boolean alreadyBooked) {
}
