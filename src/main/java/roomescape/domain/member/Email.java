package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.InvalidMemberException;

@Embeddable
public class Email {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^\\w+@\\w+\\.\\w+$");

    private String value;

    public Email() {
    }

    public Email(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!EMAIL_REGEX.matcher(value).matches()) {
            throw new InvalidMemberException("유효하지 않은 이메일입니다.");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
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

    public String getValue() {
        return value;
    }
}
