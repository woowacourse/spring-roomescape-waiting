package roomescape.theme.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.DomainValidationException;

public class Description {

    @Column(name = "description", nullable = false)
    private String value;

    public Description(String value) {
        validate(value);
        this.value = value;
    }

    protected Description() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new DomainValidationException("테마 설명은 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new DomainValidationException("테마 설명은 공백문자로만 이루어질 수 없습니다");
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
        Description that = (Description) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
