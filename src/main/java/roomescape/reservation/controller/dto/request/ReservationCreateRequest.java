package roomescape.reservation.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReservationCreateRequest(
    @NotBlank(message = "이름을 입력해야 합니다.")
    @Size(max = 10, message = "이름은 10글자 이하이어야 합니다.")
    String name,

    @NotNull(message = "예약일을 입력해야 합니다.")
    LocalDate date,

    @NotNull(message = "예약 시간을 선택해야 합니다.")
    Long timeId,

    @NotNull(message = "테마를 선택해야 합니다.")
    Long themeId
) {
}
