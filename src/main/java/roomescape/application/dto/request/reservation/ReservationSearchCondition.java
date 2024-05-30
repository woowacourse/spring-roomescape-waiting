package roomescape.application.dto.request.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationSearchCondition(
        @NotNull(message = "시작일은 비어있을 수 없습니다.") LocalDate start,
        @NotNull(message = "종료일은 비어있을 수 없습니다.") LocalDate end,
        @Positive(message = "예약자명은 비어있을 수 없습니다.") Long memberId,
        @Positive(message = "테마명은 비어있을 수 없습니다.") Long themeId
) {
}
