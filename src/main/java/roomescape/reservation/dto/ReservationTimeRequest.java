package roomescape.reservation.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeRequest(
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public ReservationTime toEntity() {
        return ReservationTime.withoutId(startAt);
    }
}
