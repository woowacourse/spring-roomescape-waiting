package roomescape.theme.domain;

import io.micrometer.common.util.StringUtils;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

import java.util.Objects;

public class ThemeName {
    private final String value;

    public ThemeName(final String value) {
        this.value = value;

        validateBlank();
    }

    private void validateBlank() {
        if (StringUtils.isBlank(value)) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("테마 이름(ThemeName)에 유효하지 않은 값(null OR 공백)이 입력되었습니다. [values : %s]", this));
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
