package roomescape.exception;

public class ThemeNotFoundException extends RuntimeException {

    public ThemeNotFoundException(Long id) {
        super(id + "번 테마를 찾을 수 없습니다.");
    }
}
