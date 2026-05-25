package roomescape.time.exception;

import roomescape.global.exception.DuplicateException;

public class DuplicateTimeException extends DuplicateException {

    public DuplicateTimeException() {
        super("예약 시간이 이미 존재합니다.");
    }
}
