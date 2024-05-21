package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimeBookedResponse(Long id, LocalTime startAt, Boolean alreadyBooked) {

    public static TimeBookedResponse from(ReservationTime reservationTime) {
        return new TimeBookedResponse(reservationTime.getId(), reservationTime.getStartAt(), null);
    }

    public static TimeBookedResponse of(ReservationTime reservationTime, Boolean alreadyBooked) {
        return new TimeBookedResponse(reservationTime.getId(), reservationTime.getStartAt(), alreadyBooked);
    }
}
