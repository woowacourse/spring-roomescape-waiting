package roomescape.theme.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class ThemeName {

    public static final int THEME_NAME_LENGTH_LIMIT = 20;
    private String name;

    public ThemeName(final String name) {
        validateName(name);
        this.name = name;
    }

    public ThemeName() {

    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > THEME_NAME_LENGTH_LIMIT) {
            throw new IllegalArgumentException("테마 이름은 최소 1글자, 최대 20글자여야합니다.");
        }
    }

    public String getValue() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ThemeName themeName = (ThemeName) o;
        return Objects.equals(name, themeName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
