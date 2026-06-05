package roomescape.time.exception;

import org.springframework.http.HttpStatus;

import roomescape.global.error.BusinessException;
import roomescape.global.error.ErrorCode;

public class TimeNotFoundException extends BusinessException {
    public TimeNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.TIME_NOT_FOUND, "예약 시간을 찾을 수 없습니다. id=" + id);
    }
}
