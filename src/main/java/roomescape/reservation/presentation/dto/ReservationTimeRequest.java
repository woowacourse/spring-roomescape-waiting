package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class ReservationTimeRequest {

    @NotNull
    private final LocalTime startAt;

    public ReservationTimeRequest(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
