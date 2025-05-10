package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservationTimeWithIsBookedGetResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean isBooked) {
}
