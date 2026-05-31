package roomescape.exception;

public class ThemeNotFoundException extends RoomescapeException {

    public ThemeNotFoundException(long id) {
        super("THEME_NOT_FOUND", "해당 식별자의 테마를 찾을 수 없습니다. id: " + id);
    }
}
