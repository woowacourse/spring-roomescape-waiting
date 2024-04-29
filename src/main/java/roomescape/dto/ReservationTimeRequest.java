package roomescape.dto;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

import static roomescape.dto.InputValidator.validateNotNull;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        validateNotNull(startAt);
    }

    public ReservationTime toReservationTime() {
        return new ReservationTime(this.startAt());
    }
}
