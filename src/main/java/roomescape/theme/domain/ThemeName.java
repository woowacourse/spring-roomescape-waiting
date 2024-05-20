package roomescape.theme.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.DomainValidationException;

public class ThemeName {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 20;

    @Column(name = "name", nullable = false)
    private String value;

    public ThemeName(String value) {
        validate(value);
        this.value = value;
    }

    protected ThemeName() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
        validateLength(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new DomainValidationException("테마 이름은 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new DomainValidationException("테마 이름은 공백문자로만 이루어질 수 없습니다");
        }
    }

    private void validateLength(String value) {
        int nameLength = value.length();
        if (nameLength < MIN_LENGTH || nameLength > MAX_LENGTH) {
            throw new DomainValidationException("테마 이름의 길이는 " + MIN_LENGTH + "자 이상, " + MAX_LENGTH + "자 이하여야 합니다");
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
}

