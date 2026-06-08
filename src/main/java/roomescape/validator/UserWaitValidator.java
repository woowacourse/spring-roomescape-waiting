package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Wait;
import roomescape.exception.custom.CannotDeletePastWaitException;

public class UserWaitValidator implements WaitValidator {

    @Override
    public void validateDelete(Wait wait, LocalDateTime now) {
        if (wait.isPast(now)) {
            throw new CannotDeletePastWaitException();
        }
    }
}
