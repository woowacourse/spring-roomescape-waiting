package roomescape.domain.theme;

import roomescape.common.exception.BadRequestException;

import java.util.Objects;

public class ThemeName {
    private static final int MIN_NAME_LENGTH = 1;
    private static final int MAX_NAME_LENGTH = 30;
    private final String value;

    public ThemeName(String value) {
        Objects.requireNonNull(value);
        String striped = value.strip();
        validate(striped);
        this.value = striped;
    }

    private void validate(String value) {
        if (MIN_NAME_LENGTH > value.length() || MAX_NAME_LENGTH < value.length()) {
            throw new BadRequestException("테마 이름 길이는 1자 ~ 50자 사이여야 합니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
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
