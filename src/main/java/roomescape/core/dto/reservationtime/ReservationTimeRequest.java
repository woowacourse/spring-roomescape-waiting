package roomescape.core.dto.reservationtime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReservationTimeRequest {
    @NotBlank(message = "시작 시간은 비어있을 수 없습니다.")
    @Pattern(regexp = "^([0-1]\\d|2[0-3]):[0-5]\\d$", message = "시작 시간은 HH:mm 형식이어야 합니다.")
    private String startAt;

    public ReservationTimeRequest() {
    }

    public ReservationTimeRequest(final String startAt) {
        this.startAt = startAt;
    }

    public String getStartAt() {
        return startAt;
    }
}
