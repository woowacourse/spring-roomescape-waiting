package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeSaveRequest(
        @NotNull(message = "추가할 예약시간이 존재하지 않습니다.")
        LocalTime startAt
) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
