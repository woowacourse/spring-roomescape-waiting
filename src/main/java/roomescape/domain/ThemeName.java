package roomescape.domain;

public record ThemeName(
        String name
) {

    public ThemeName {
        validate(name);
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 비워둘 수 없습니다.");
        }
    }
}
