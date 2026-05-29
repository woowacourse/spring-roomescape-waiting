package roomescape.waiting.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.WaitingRepository;

@Component
public class WaitingValidator {

    private final WaitingRepository waitingRepository;

    public WaitingValidator(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public void validateAlreadyMyWaiting(WaitingCreateCommand createCommand) {
        boolean exists = waitingRepository
                .findByNameAndDateAndTimeIdAndThemeId(
                        createCommand.name(),
                        createCommand.date(),
                        createCommand.timeId(),
                        createCommand.themeId()
                ).isPresent();
        if (exists) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }
}
