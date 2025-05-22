package roomescape.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.reservationtime.ReservationTime;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        Boolean alreadyBooked
) {

    public AvailableReservationTimeResponse {
        if (id == null || startAt == null || alreadyBooked == null) {
            throw new ResponseInvalidException();
        }
    }

    public static AvailableReservationTimeResponse from(ReservationTime reservationTime, boolean alreadyBooked) {
        return new AvailableReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt(),
                alreadyBooked
        );
    }
}
