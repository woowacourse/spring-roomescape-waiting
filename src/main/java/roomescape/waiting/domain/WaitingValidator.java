package roomescape.waiting.domain;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.waiting.application.dto.WaitingCreateCommand;

@Component
public class WaitingValidator {

    private final WaitingRepository waitingRepository;

    public WaitingValidator(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public void validateAlreadyWaiting(WaitingCreateCommand createCommand) {
        boolean exists = waitingRepository
                .findByDateAndTimeIdAndThemeId(createCommand.date(), createCommand.timeId(), createCommand.themeId())
                .isPresent();
        if (exists) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }
}
