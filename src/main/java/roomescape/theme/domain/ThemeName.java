package roomescape.theme.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;

@Embeddable
public final class ThemeName {

    private String name;

    public ThemeName(final String name) {
        validateName(name);
        this.name = name;
    }

    protected ThemeName() {

    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 20) {
            throw new BadRequestException("테마 이름은 최소 1글자, 최대 20글자여야합니다.");
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
