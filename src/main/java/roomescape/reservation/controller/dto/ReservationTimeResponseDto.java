package roomescape.reservation.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeResponseDto(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
) {

    public static ReservationTimeResponseDto from(ReservationTime time) {
        if (time == null) {
            return null;
        }
        return new ReservationTimeResponseDto(time.getId(), time.getStartAt(), time.getEndAt());
    }
}
