package roomescape.theme.domain;

public class ThemeName {

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
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
    }

    public String getName() {
        return name;
    }
}
