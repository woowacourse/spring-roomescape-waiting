package roomescape.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class Password {

    @Column(name = "password", nullable = false)
    private String value;

    public Password() {

    }

    public Password(String value) {
        validateNullAndBlank(value);
        this.value = value;
    }

    private void validateNullAndBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new ValueNullOrEmptyException("비밀번호는 비어있을 수 없습니다.");
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
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Password{" +
                "value='" + value + '\'' +
                '}';
    }
}
