package roomescape.domain.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class Description {

    @Column(name = "description", nullable = false)
    private String value;

    public Description() {

    }

    public Description(String value) {
        validateNullAndBlank(value);
        this.value = value;
    }

    private void validateNullAndBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new ValueNullOrEmptyException("테마 설명은 비어있을 수 없습니다.");
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

    @Override
    public String toString() {
        return "Description{" +
                "value='" + value + '\'' +
                '}';
    }
}
