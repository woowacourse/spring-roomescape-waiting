package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.domain.TimeSlot;

public record ReservationTimeRequest(
        @NotNull(message = "startAt 값이 없습니다.") LocalTime startAt
) {
    public TimeSlot toTime() {
        return TimeSlot.createWithoutId(startAt);
    }
}
