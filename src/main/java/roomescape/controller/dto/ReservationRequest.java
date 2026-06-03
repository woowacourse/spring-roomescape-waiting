package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름이 입력되지 않았습니다. 이름을 입력해주세요.")
        String name,
        @NotNull(message = "날짜가 입력되지 않았습니다. 날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "예약 시간이 선택되지 않았습니다. 시간을 선택해주세요.")
        Long timeId,
        @NotNull(message = "테마가 선택되지 않았습니다. 테마를 선택해주세요.")
        Long themeId
) {
}
