package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeCreateRequestDto(@JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTime createWithoutId() {
        return new ReservationTime(null, startAt);
    }
}
