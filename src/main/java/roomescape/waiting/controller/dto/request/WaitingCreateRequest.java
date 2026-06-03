package roomescape.waiting.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingCreateRequest(
    @NotBlank(message = "이름을 입력해야 합니다.")
    String name,

    @NotNull(message = "예약일을 입력해야 합니다.")
    LocalDate date,

    @NotNull(message = "예약 시간을 선택해야 합니다.")
    Long timeId,

    @NotNull(message = "테마를 선택해야 합니다.")
    Long themeId
) {
}
