package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "예약 시작 시간을 입력해 주세요.")
        LocalTime startAt
) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
