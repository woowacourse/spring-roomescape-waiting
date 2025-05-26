package roomescape.exception.custom.reason.waiting;

public class WaitingNotFoundException extends RuntimeException {

    public WaitingNotFoundException() {
        super("대기 내역이 존재하지 않습니다.");
    }
}
