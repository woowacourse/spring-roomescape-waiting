package roomescape.application.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record ReservationTimeRequest(
        @NotNull(message = "시간을 입력해주세요.")
        LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
