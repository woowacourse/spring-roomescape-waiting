package roomescape.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.reservationtime.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
    public ReservationTimeResponse {
        if (id == null || startAt == null) {
            throw new ResponseInvalidException();
        }
    }

    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
