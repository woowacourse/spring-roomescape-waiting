package roomescape.service.dto.response;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationStatusResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

    public ReservationStatusResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
