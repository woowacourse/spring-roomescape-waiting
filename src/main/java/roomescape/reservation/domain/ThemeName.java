package roomescape.reservation.domain;

public class ThemeName {
    private static final int MAX_THEME_NAME_LENGTH = 30;
    private final String value;

    public ThemeName(final String value) {
        validateName(value);
        this.value = value;
    }

    private void validateName(final String value) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 이름을 입력해주세요.");
        }
        if (value.length() > MAX_THEME_NAME_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 테마 이름의 길이는 " + MAX_THEME_NAME_LENGTH + "자 이하입니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
