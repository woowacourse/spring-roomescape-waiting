package roomescape.domain.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

public record ReservationTimeRequest(LocalTime startAt) {
    public ReservationTimeRequest {
        isValid(startAt);
    }

    private void isValid(LocalTime startAt) {
        if (startAt == null) {
            throw new EmptyValueNotAllowedException("startAt");
        }
    }

    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }
}
