package roomescape.reservation.exception;

public class DuplicateTimeException extends RuntimeException {

    public DuplicateTimeException() {
        super("이미 존재하는 시간이다.");
    }
}
