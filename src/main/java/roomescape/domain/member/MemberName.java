package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.InvalidRequestException;

@Embeddable
public class MemberName {

    @Column(name = "name")
    private String value;

    public MemberName() {
    }

    public MemberName(String value) {
        validateNullOrBlank(value);
        this.value = value;
    }

    private void validateNullOrBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("이름을 입력해주세요.");
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
        MemberName that = (MemberName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
