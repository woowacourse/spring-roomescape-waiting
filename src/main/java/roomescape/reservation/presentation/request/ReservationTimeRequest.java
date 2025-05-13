package roomescape.reservation.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeRequest(
        @NotNull LocalTime startAt
) {
}
