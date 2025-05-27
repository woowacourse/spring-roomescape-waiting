package roomescape.user.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "날짜는 공백을 허용하지 않습니다.")
        LocalDate date,
        @NotNull(message = "테마는 공백을 허용하지 않습니다.")
        Long themeId,
        @NotNull(message = "시간은 공백을 허용하지 않습니다.")
        Long timeId
) {
}
