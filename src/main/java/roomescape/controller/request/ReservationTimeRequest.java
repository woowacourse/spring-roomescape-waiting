package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class ReservationTimeRequest {

    @NotNull(message = "예약 시간은 null일 수 없습니다.")
    private LocalTime startAt;

    public ReservationTimeRequest(LocalTime startAt) {
        this.startAt = startAt;
    }

    private ReservationTimeRequest() {
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
