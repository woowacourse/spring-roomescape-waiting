package roomescape.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class Email {

    protected static final String EMAIL_EMPTY_ERROR_MESSAGE = "email은 비어있을 수 없습니다.";

    @Column(name = "email", nullable = false)
    private String value;

    public Email() {

    }

    public Email(String value) {
        validateNullAndBlank(value);
        this.value = value;
    }

    private void validateNullAndBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new ValueNullOrEmptyException(EMAIL_EMPTY_ERROR_MESSAGE);
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

    @Override
    public String toString() {
        return "Email{" +
                "value='" + value + '\'' +
                '}';
    }
}
