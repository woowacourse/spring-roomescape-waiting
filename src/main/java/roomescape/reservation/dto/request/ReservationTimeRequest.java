package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record ReservationTimeRequest(
        @NotNull(message = "startAt 값이 없습니다.") LocalTime startAt
) {
    public ReservationTime toTime() {
        return ReservationTime.createWithoutId(startAt);
    }
}
