package roomescape.exception.business;

import org.springframework.http.HttpStatus;

public class PastTimeCancelException extends BusinessException {

    public PastTimeCancelException() {
        super(HttpStatus.BAD_REQUEST, "이미 지난 예약은 취소할 수 없습니다.");
    }
}
