package roomescape.time.dto;

import java.time.LocalTime;
import roomescape.time.domain.Time;

public record TimeResponse(long id, LocalTime startAt) {

    public static TimeResponse toResponse(Time time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }

}
