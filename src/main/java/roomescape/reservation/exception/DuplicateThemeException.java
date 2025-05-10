package roomescape.reservation.exception;

public class DuplicateThemeException extends RuntimeException {

    public DuplicateThemeException() {
        super("해당 이름을 가진 테마가 이미 존재한다.");
    }
}
