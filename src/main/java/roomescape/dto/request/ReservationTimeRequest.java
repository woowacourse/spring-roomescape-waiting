package roomescape.dto.request;

import static roomescape.dto.InputValidator.validateNotNull;

import java.time.LocalTime;

import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        validateNotNull(startAt);
    }

    public ReservationTime toReservationTime() {
        return new ReservationTime(this.startAt());
    }
}
