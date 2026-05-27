package roomescape.wating.domain.exception;

import roomescape.common.exception.UnprocessableContentException;

public class PastReservationWaitingCancellationException extends UnprocessableContentException {

    public PastReservationWaitingCancellationException() {
        super("과거 시간 예약의 대기를 삭제할 수 없습니다.");
    }
}
