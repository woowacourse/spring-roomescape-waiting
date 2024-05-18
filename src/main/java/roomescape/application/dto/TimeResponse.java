package roomescape.application.dto;

import java.time.LocalTime;
import roomescape.domain.Time;

public record TimeResponse(
        Long id,
        LocalTime startAt
) {

    public static TimeResponse from(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }
}
