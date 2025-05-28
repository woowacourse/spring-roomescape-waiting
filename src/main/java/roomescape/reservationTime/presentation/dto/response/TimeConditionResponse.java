package roomescape.reservationTime.presentation.dto.response;

import java.time.LocalTime;

public record TimeConditionResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
}
