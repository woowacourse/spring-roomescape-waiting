package roomescape.waiting.application.dto;

import static roomescape.global.validation.ValidationUtils.requireNotBlank;
import static roomescape.global.validation.ValidationUtils.requireNotNull;

import java.time.LocalDate;
import roomescape.global.exception.WaitingErrorCode;

public record WaitingCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public WaitingCreateCommand {
        requireNotBlank(name, WaitingErrorCode.WAITING_NAME_REQUIRED);
        requireNotNull(date, WaitingErrorCode.WAITING_DATE_REQUIRED);
        requireNotNull(timeId, WaitingErrorCode.WAITING_TIME_REQUIRED);
        requireNotNull(themeId, WaitingErrorCode.WAITING_THEME_REQUIRED);
    }
}
