package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import roomescape.member.exception.EmailException;

@Embeddable
public record Email(String email) {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public Email {
        validateEmailIsNonBlank(email);
        validateEmailFormat(email);
    }

    private static void validateEmailIsNonBlank(final String email) {
        if (email == null || email.isEmpty()) {
            throw new EmailException("이메일은 비어있을 수 없습니다.");
        }
    }

    private static void validateEmailFormat(String email) {
        if (!email.matches(EMAIL_REGEX)) {
            throw new EmailException("이메일 형식이 맞지 않습니다.");
        }
    }
}
