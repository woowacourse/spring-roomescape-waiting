package roomescape.admin.controller.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReserveByAdminRequest(
        @NotNull(message = "예약 날짜를 선택해주세요.") LocalDate date,
        @NotNull(message = "테마를 선택해주세요.") Long themeId,
        @NotNull(message = "예약 시간을 선택해주세요.") Long timeId,
        @NotNull(message = "예약할 회원을 선택해주세요.") Long memberId
) {
}
