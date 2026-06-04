package roomescape.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ReservationTimeRequest(
        @NotNull(message = "시작 시간은 비어있을 수 없습니다")
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
}
