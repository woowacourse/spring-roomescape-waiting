package roomescape.theme.domain;

public class ThemeName {

    private static final String NAME_REQUIRED_MESSAGE = "이름을 입력해야 합니다.";

    private final String name;

    private ThemeName(final String value) {
        validate(value);
        this.name = value;
    }

    public static ThemeName from(final String value) {
        return new ThemeName(value);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(NAME_REQUIRED_MESSAGE);
        }
    }

    public String getName() {
        return name;
    }
}
