package roomescape.exception;

public class ThemeNotFoundException extends RoomescapeException {

    public ThemeNotFoundException() {
        super("THEME_NOT_FOUND", "해당 테마를 찾을 수 없습니다.");
    }
}
