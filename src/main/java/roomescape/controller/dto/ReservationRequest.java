package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,
        @NotNull(message = "유효한 시간대 번호가 필요합니다.")
        Long timeId,
        @NotNull(message = "유효한 테마 번호가 필요합니다.")
        Long themeId
) {
}
