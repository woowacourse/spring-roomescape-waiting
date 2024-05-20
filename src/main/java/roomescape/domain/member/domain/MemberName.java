package roomescape.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.ValueLengthException;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class MemberName {

    private static final int NAME_MAX_LENGTH = 10;
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String value;

    public MemberName() {

    }

    public MemberName(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        validateNullAndBlank(value);
        validateLength(value);
    }

    private void validateNullAndBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new ValueNullOrEmptyException("멤버이름은 비어있을 수 없습니다.");
        }
    }

    private void validateLength(String value) {
        if (value.length() > 10) {
            throw new ValueLengthException("멤버의 이름은 " + NAME_MAX_LENGTH + "을 초과할 수 없습니다.");
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
        MemberName name1 = (MemberName) o;
        return Objects.equals(value, name1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
