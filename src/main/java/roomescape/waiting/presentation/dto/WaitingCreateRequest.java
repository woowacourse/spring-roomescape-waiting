package roomescape.waiting.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.waiting.application.dto.WaitingCreateCommand;

public record WaitingCreateRequest(
        @NotNull(message = "날짜를 입력해주세요.") LocalDate date,
        @NotNull(message = "시간을 입력해주세요.") Long timeId,
        @NotNull(message = "테마를 입력해주세요.") Long themeId
) {

    public WaitingCreateCommand convertToCreateCommand(final long memberId) {
        return new WaitingCreateCommand(date, timeId, themeId, memberId);
    }
}
