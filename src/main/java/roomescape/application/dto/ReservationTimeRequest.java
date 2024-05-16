package roomescape.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(@NotNull LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
