package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record TimeInfo(
        Long id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        LocalTime startAt
) {
    public static TimeInfo from(ReservationTime time) {
        return new TimeInfo(time.id(), time.startAt());
    }
}
