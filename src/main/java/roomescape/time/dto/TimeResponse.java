package roomescape.time.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record TimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
    public static TimeResponse from(ReservationTime time) {
        return new TimeResponse(time.getId(), time.getStartAt());
    }
}
