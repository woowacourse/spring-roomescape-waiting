package roomescape.time.exception;

import org.springframework.http.HttpStatus;

import roomescape.error.BusinessException;
import roomescape.error.ErrorCode;

public class ReservationTimeConflictException extends BusinessException {
    public ReservationTimeConflictException(Long id) {
        super(HttpStatus.CONFLICT, ErrorCode.TIME_IN_USE, "해당 시간에 예약이 존재하여 삭제할 수 없습니다. id=" + id);
    }
}
