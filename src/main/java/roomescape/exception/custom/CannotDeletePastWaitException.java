package roomescape.exception.custom;

public class CannotDeletePastWaitException extends RuntimeException {

    public CannotDeletePastWaitException() {
        super("지나간 시간의 대기는 삭제할 수 없습니다.");
    }
}
