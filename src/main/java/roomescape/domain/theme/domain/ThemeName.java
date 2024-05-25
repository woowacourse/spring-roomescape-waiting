package roomescape.domain.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class ThemeName {

    protected static final String THEME_NAME_EMPTY_ERROR_MESSAGE = "테마 이름은 비어있을 수 없습니다.";

    @Column(name = "name", nullable = false)
    private String value;

    public ThemeName() {

    }

    public ThemeName(String value) {
        validateNullAndBlank(value);
        this.value = value;
    }

    private void validateNullAndBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new ValueNullOrEmptyException(THEME_NAME_EMPTY_ERROR_MESSAGE);
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
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ThemeName{" +
                "value='" + value + '\'' +
                '}';
    }
}
