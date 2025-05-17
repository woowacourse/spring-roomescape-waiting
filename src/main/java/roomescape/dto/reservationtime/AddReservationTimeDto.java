package roomescape.dto.reservationtime;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record AddReservationTimeDto(@NotNull LocalTime startAt) {

    public ReservationTime toEntity() {
        return new ReservationTime(null, startAt);
    }
}

