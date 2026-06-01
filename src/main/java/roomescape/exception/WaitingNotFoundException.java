package roomescape.exception;

public class WaitingNotFoundException extends RuntimeException {

    public WaitingNotFoundException(Long id) {
        super(id + "번 대기열이 존재하지 않습니다.");
    }
}
