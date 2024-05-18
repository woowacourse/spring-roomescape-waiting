package roomescape.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.Time;

public record TimeRequest(@NotNull LocalTime startAt) {

    public Time toReservationTime() {
        return new Time(startAt);
    }
}
