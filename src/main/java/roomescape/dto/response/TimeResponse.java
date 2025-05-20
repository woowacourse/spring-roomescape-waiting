package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.entity.ReservationTime;

public record TimeResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm") LocalTime startAt,
    Boolean alreadyBooked) {

    public static TimeResponse from(ReservationTime reservationTime, Boolean alreadyBooked) {
        return new TimeResponse(
            reservationTime.getId(),
            reservationTime.getStartAt(),
            alreadyBooked);
    }

    public static TimeResponse from(ReservationTime reservationTime) {
        return new TimeResponse(
            reservationTime.getId(),
            reservationTime.getStartAt(),
            null);
    }
}
