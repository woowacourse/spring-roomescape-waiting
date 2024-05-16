package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@Embeddable
public class Email {
    private static final String EMAIL_REGEX = "^.*@.*\\..*$";
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    private String email;

    public Email(String email) {
        validate(email);
        this.email = email;
    }

    public Email() {
    }

    public void validate(String email) {
        if (email == null || !pattern.matcher(email).matches()) {
            throw new BusinessException(ErrorType.EMAIL_FORMAT_ERROR);
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Email email1 = (Email) o;
        return Objects.equals(getEmail(), email1.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail());
    }

    @Override
    public String toString() {
        return "Email{" +
                "email='" + email + '\'' +
                '}';
    }
}

