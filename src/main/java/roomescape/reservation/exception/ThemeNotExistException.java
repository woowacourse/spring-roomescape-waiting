package roomescape.reservation.exception;

public class ThemeNotExistException extends RuntimeException {

    public ThemeNotExistException() {
        super("테마를 찾을 수 없다.");
    }
}
