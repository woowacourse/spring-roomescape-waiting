package roomescape.domain.repository.dto;

import java.time.LocalTime;

public record TimeDataWithBookingInfo(
        long id,
        LocalTime startAt,
        boolean alreadyBooked
) {
}
