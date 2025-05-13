package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.entity.ReservationTime;

public class ReservationTimeRequest {

    public record ReservationTimeCreateRequest(
            @NotNull LocalTime startAt
    ) {
        public ReservationTime toEntity() {
            return new ReservationTime(0L, startAt);
        }
    }
}