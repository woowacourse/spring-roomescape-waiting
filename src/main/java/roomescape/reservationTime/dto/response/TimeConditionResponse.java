package roomescape.reservationTime.dto.response;

import java.time.LocalTime;

public record TimeConditionResponse(Long id, LocalTime startAt, boolean alreadyBooked) {
}
