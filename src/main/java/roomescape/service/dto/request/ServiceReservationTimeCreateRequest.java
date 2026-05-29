package roomescape.service.dto.request;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ServiceReservationTimeCreateRequest(
        LocalTime startAt
) {
    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }
}
