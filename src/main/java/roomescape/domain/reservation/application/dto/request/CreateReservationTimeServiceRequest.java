package roomescape.domain.reservation.application.dto.request;

import java.time.LocalTime;
import roomescape.domain.reservation.model.entity.ReservationTime;

public record CreateReservationTimeServiceRequest(
        LocalTime startAt
) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
