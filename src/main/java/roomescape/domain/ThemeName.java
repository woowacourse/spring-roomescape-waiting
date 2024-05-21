package roomescape.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ThemeName {

    private static final int MAX_NAME_LENGTH = 20;

    private String value;

    protected ThemeName() {
    }

    public ThemeName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String name) {
        validateLength(name);
        validateBlank(name);
    }

    private void validateLength(String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 이름은 %s 이하로 입력해야 합니다.", MAX_NAME_LENGTH));
        }
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수로 입력해야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemeName themeName = (ThemeName) o;
        return Objects.equals(value, themeName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
