package roomescape.reservation.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.time.domain.ReservationTime;

public record ReservationTimeResponse(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
) {

    public static ReservationTimeResponse from(ReservationTime time) {
        if (time == null) {
            return null;
        }
        return new ReservationTimeResponse(time.getId(), time.getStartAt(), time.getEndAt());
    }
}
