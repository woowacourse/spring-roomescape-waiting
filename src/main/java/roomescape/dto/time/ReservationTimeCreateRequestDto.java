package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeCreateRequestDto(@JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTime createWithoutId() {
        return new ReservationTime(null, startAt);
    }
}
