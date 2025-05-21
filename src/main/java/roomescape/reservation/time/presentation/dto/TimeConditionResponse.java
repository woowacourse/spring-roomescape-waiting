package roomescape.reservation.time.presentation.dto;

import java.time.LocalTime;

public record TimeConditionResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
}
