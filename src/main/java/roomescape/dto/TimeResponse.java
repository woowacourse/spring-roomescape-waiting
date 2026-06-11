package roomescape.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Time;

public record TimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {
    public static TimeResponse from(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }
}
