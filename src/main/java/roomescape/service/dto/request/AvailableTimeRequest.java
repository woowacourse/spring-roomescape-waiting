package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AvailableTimeRequest(
        @NotNull(message = "테마 ID를 입력해주세요.")
        Long themeId,
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date) {
}
