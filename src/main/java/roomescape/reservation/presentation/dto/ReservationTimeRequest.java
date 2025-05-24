package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class ReservationTimeRequest {

    @NotNull(message = "시작 시간은 반드시 입력해야 합니다")
    private final LocalTime startAt;

    public ReservationTimeRequest(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
