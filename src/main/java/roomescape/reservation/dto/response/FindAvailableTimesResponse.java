package roomescape.reservation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.model.ReservationTime;

public record FindAvailableTimesResponse(Long id, LocalTime startAt, Boolean alreadyBooked) {
    public static FindAvailableTimesResponse from(ReservationTime reservationTime, Boolean alreadyBooked) {
        return new FindAvailableTimesResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked);
    }
}
