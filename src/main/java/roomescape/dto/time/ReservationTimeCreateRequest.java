package roomescape.dto.time;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "[ERROR] 예약시간이 없습니다.") LocalTime startAt
) {

    public ReservationTime createWithoutId() {
        return new ReservationTime(null, startAt);
    }
}
