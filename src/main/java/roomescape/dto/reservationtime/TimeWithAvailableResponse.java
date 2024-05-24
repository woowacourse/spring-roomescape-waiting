package roomescape.dto.reservationtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.time.ReservationTime;

public record TimeWithAvailableResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {

    public static TimeWithAvailableResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new TimeWithAvailableResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }
}
