package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationConditionRequest(
        @NotNull(message = "테마 ID를 입력해주세요.")
        Long themeId,
        @NotNull(message = "멤바 ID를 입력해주세요.")
        Long memberId,
        @NotNull(message = "시작 날짜를 입력해주세요.")
        LocalDate dateFrom,
        @NotNull(message = "종료 날짜를 입력해주세요.")
        LocalDate dateTo
) {
}
