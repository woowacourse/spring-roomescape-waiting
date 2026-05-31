package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeRequest(
        @NotNull(message = "시간은 비어 있을 수 없습니다.")
        LocalTime startAt
) {
}
