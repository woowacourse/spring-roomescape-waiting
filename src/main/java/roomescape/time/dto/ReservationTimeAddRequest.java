package roomescape.time.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeAddRequest(
        @NotNull(message = "예약 시간은 필수입니다.") LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(null, startAt);
    }
}
