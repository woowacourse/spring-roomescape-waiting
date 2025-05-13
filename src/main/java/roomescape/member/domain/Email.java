package roomescape.member.domain;

import java.util.Objects;
import roomescape.member.exception.EmailException;

public class Email {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private final String email;

    public Email(String email) {
        validateEmailIsNonBlank(email);
        validateEmailFormat(email);

        this.email = email;
    }

    private void validateEmailIsNonBlank(final String email) {
        if (email == null || email.isEmpty()) {
            throw new EmailException("이메일은 비어있을 수 없습니다.");
        }
    }

    private void validateEmailFormat(String email) {
        if (!email.matches(EMAIL_REGEX)) {
            throw new EmailException("이메일 형식이 맞지 않습니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Email email1)) {
            return false;
        }
        return Objects.equals(getEmail(), email1.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEmail());
    }
}
