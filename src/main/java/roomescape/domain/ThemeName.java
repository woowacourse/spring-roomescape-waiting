package roomescape.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.theme.InvalidThemeNameLengthException;

@Embeddable
public class ThemeName {
    private static final int MAX_LENGTH = 16;

    private String name;

    protected ThemeName() {
    }

    public ThemeName(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name.length() > MAX_LENGTH) {
            throw new InvalidThemeNameLengthException();
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemeName that = (ThemeName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
