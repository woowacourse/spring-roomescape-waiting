package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.exception.EmailRequiredException;
import roomescape.member.exception.InvalidEmailException;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class Email {

    private static final String VALID_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";


    @Column(name = "email", nullable = false)
    private String value;

    public Email(String value) {
        validateEmail(value);
        this.value = value;
    }

    private void validateEmail(String email) {
        validateNotBlank(email);
        validateRegex(email);
    }

    private void validateNotBlank(String email) {
        if (email == null || email.isBlank()) {
            throw new EmailRequiredException();
        }
    }

    private void validateRegex(String email) {
        if (!email.matches(VALID_EMAIL_REGEX)) {
            throw new InvalidEmailException();
        }
    }
}
