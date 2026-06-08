package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Wait;

public interface WaitValidator {

    void validateDelete(Wait wait, LocalDateTime now);
}
