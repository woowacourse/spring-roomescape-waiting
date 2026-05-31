package roomescape.time.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.time.domain.ReservationTime;

public record TimeResponse(
        Long id,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
) {

    public static TimeResponse from(ReservationTime time) {
        return new TimeResponse(time.getId(), time.getStartAt(), time.getEndAt());
    }
}
