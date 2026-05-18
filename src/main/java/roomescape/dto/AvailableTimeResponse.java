package roomescape.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Time;

public record AvailableTimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean isAvailable) {
    public static AvailableTimeResponse from(Time time, boolean isAvailable) {
        return new AvailableTimeResponse(time.getId(), time.getStartAt(), isAvailable);
    }

}
