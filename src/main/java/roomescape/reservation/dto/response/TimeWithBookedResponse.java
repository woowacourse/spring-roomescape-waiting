package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record TimeWithBookedResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {
    public static TimeWithBookedResponse of(ReservationTime time, boolean alreadyBooked) {
        return new TimeWithBookedResponse(
                time.getId(), time.getStartAt(), alreadyBooked);
    }
}
