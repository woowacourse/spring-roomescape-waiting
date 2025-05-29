package roomescape.waiting.controller.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRequest(
        @NotNull(message = "예약 날짜를 입력해주세요.") LocalDate date,
        @NotNull(message = "예약 시간을 선택해주세요.") Long timeId,
        @NotNull(message = "테마를 선택해주세요.") Long themeId
) {
}
