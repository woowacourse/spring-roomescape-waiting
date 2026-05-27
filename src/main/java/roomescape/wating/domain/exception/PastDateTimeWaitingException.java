package roomescape.wating.domain.exception;

import roomescape.common.exception.UnprocessableContentException;

public class PastDateTimeWaitingException extends UnprocessableContentException {

    public PastDateTimeWaitingException() {
        super("과거 시간의 예약에 대기를 등록할 수 없습니다.");
    }
}
