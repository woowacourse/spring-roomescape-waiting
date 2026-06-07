package roomescape.exception.custom;

public class AlreadyWaitingException extends CustomException {

    public AlreadyWaitingException() {
        super("이미 해당 슬롯에 예약 대기를 신청했습니다.");
    }
}
