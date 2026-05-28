package roomescape.exception;

public class ExpiredDateTimeException extends RuntimeException {

    public ExpiredDateTimeException() {
        super("이미 지난 날짜이거나 시간입니다.");
    }
}
