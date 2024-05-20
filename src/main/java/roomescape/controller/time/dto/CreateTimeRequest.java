package roomescape.controller.time.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record CreateTimeRequest(
        @NotNull
        LocalTime startAt) {

    public ReservationTime toDomain() {
        return new ReservationTime(null, startAt);
    }
}
