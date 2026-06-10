package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Wait;

public class AdminWaitValidator implements WaitValidator {

    @Override
    public void validateDelete(Wait wait, LocalDateTime now) {
        // 현재 관리자 대기 삭제 검증은 X
    }
}
