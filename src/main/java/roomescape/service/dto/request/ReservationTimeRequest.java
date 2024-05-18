package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(
        @NotNull(message = "시간에 빈값을 입력할 수 없습니다.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
