package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class ReservationTimeCreateRequest {
    @NotNull(message = "시간을 입력해야 합니다")
    private final LocalTime startAt;

    public ReservationTimeCreateRequest(LocalTime startAt) {
        this.startAt = startAt;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
