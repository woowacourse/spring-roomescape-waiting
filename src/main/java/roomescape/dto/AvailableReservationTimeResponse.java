package roomescape.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationTime;

public record AvailableReservationTimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean isAvailable) {
    public static AvailableReservationTimeResponse from(ReservationTime time, boolean isAvailable) {
        return new AvailableReservationTimeResponse(time.getId(), time.getStartAt(), isAvailable);
    }

}
