package roomescape.service.dto.response;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record BookingStatusResponse(LocalTime startAt, Long timeId, boolean alreadyBooked) {

    public BookingStatusResponse(ReservationTime reservationTime, boolean alreadyBooked) {
        this(reservationTime.getStartAt(), reservationTime.getId(), alreadyBooked);
    }
}
