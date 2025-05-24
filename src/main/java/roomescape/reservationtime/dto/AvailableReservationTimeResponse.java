package roomescape.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.reservationtime.domain.ReservationTime;

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
