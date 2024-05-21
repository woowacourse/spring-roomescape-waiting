package roomescape.service.dto.response;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeBookedResponse(
        Long id,
        LocalTime startAt,
        boolean alreadyBooked
) {

    public static ReservationTimeBookedResponse of(ReservationTime time, boolean alreadyBooked) {
        return new ReservationTimeBookedResponse(time.getId(), time.getStartAt(), alreadyBooked);
    }
}
