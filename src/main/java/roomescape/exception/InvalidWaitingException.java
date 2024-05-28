package roomescape.exception;

public class InvalidWaitingException extends IllegalArgumentException {

    public InvalidWaitingException() {
        super("대기를 요청할 예약이 존재하지 않습니다.");
    }
}
