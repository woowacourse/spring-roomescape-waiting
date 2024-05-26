package roomescape.member.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.IllegalRequestException;

public class Password {

    @Column(name = "password", nullable = false)
    private String value;

    public Password(String value) {
        validate(value);
        this.value = value;
    }

    protected Password() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new IllegalRequestException("비밀번호는 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new IllegalRequestException("비밀번호는 공백문자로만 이루어질 수 없습니다");
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
}
