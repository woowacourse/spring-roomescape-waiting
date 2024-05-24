package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        @NotNull(message = "예약 시간은 필수 값입니다.")
        LocalTime startAt
) {
}
