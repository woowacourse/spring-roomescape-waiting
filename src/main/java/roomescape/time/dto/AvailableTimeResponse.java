package roomescape.time.dto;

import roomescape.time.domain.ReservationTime;

public record AvailableTimeResponse(TimeResponse time, boolean alreadyBooked) {
    public static AvailableTimeResponse of(ReservationTime time, boolean alreadyBooked) {
        return new AvailableTimeResponse(TimeResponse.from(time), alreadyBooked);
    }
}
