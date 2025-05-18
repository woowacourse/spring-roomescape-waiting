package roomescape.time.controller.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeCreateRequest(
        @NotNull(message = "시간을 입력해주세요.") LocalTime startAt) {
}
