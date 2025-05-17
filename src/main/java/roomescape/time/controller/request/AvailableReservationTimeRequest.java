package roomescape.time.controller.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AvailableReservationTimeRequest(
        @NotNull(message = "날짜를 선택해주세요.") LocalDate date,
        @NotNull(message = "테마를 선택해주세요.") Long themeId
) {
}
