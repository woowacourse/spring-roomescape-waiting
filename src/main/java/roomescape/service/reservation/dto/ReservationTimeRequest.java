package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record ReservationTimeRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalTime startAt) {

    public ReservationTime toEntity() {
        return new ReservationTime(startAt);
    }
}
