package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record CreateReservationTimeRequest(
        @NotNull(message = "시간은 비어있을 수 없습니다.") LocalTime startAt
) {

    public ReservationTime toReservationTime() {
        return new ReservationTime( startAt);
    }
}

