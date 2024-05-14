package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservationTimeInfoResponse(

        Long timeId,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,

        boolean alreadyBooked
) {
}
