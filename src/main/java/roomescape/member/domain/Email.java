package roomescape.member.domain;

import jakarta.persistence.Column;
import java.util.Objects;
import roomescape.global.exception.IllegalRequestException;

public class Email {

    private static final String EMAIL_FORMAT = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    @Column(name = "email", nullable = false)
    private String value;

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    protected Email() {
    }

    private void validate(String value) {
        validateNotNull(value);
        validateNotBlank(value);
        validateEmailFormat(value);
    }

    private void validateNotNull(String value) {
        if (value == null) {
            throw new IllegalRequestException("이메일은 비어있을 수 없습니다");
        }
    }

    private void validateNotBlank(String value) {
        if (value.isBlank()) {
            throw new IllegalRequestException("이메일은 공백문자로만 이루어질 수 없습니다");
        }
    }

    private void validateEmailFormat(String value) {
        if (!value.matches(EMAIL_FORMAT)) {
            throw new IllegalRequestException(value + "은 유효하지 않은 이메일입니다");
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
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
