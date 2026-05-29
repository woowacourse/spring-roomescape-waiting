package roomescape.waiting.domain.exception;

import roomescape.common.exception.UnprocessableContentException;

public class NoReservationForWaitingException extends UnprocessableContentException {

    public NoReservationForWaitingException() {
        super("예약이 존재하지 않는 슬롯에는 대기를 신청할 수 없습니다.");
    }
}
