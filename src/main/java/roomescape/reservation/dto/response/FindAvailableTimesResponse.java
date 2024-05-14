package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record FindAvailableTimesResponse(Long id, LocalTime startAt, Boolean alreadyBooked) {
    public static FindAvailableTimesResponse from(final ReservationTime reservationTime, final Boolean alreadyBooked) {
        return new FindAvailableTimesResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked);
    }
}
