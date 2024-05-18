package roomescape.controller.time.dto;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record CreateTimeRequest(
        @NotNull
        LocalTime startAt) {

    public ReservationTime toDomain() {
        return new ReservationTime(null, startAt);
    }
}
