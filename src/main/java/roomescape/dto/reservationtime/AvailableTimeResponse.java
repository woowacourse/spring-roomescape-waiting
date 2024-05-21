package roomescape.dto.reservationtime;

import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.time.ReservationTime;

public record AvailableTimeResponse(
        Long id,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,

        boolean alreadyBooked
) {

    public static AvailableTimeResponse createAlreadyBookedTime(ReservationTime reservationTime) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                true
        );
    }

    public static AvailableTimeResponse createAvailableTime(ReservationTime reservationTime) {
        return new AvailableTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                false
        );
    }
}
