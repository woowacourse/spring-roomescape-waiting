package roomescape.dto.request;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeRequestDto(LocalTime startAt) {

    public ReservationTime toEntity() {
        return ReservationTime.createWithoutId(startAt);
    }
}
