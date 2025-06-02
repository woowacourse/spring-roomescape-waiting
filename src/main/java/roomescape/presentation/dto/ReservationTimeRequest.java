package roomescape.presentation.dto;

import java.time.LocalTime;
import roomescape.business.domain.ReservationTime;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTime toDomain() {
        return new ReservationTime(startAt);
    }
}
