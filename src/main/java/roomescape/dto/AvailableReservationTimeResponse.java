package roomescape.dto;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(Long timeId, LocalTime startAt, boolean alreadyBooked) {

    public static AvailableReservationTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt(),
                alreadyBooked);
    }
}
