package roomescape.domain.slot.theme;

public record ThemeName(
        String value
) {
    public static final int THEME_NAME_MAX_LENGTH = 20;

    public ThemeName {
        if (value.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 비어 있을 수 없습니다.");
        }

        if (value.length() > THEME_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("테마 이름은 %d자를 초과할 수 없습니다.".formatted(THEME_NAME_MAX_LENGTH));
        }
    }

    public static ThemeName parse(String value) {
        return new ThemeName(value);
    }
}
