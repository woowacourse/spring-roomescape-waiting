package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import roomescape.global.exception.InvalidArgumentException;

@Embeddable
public record Email(
        String email
) {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static Email create(String email) {
        validateEmail(email);
        return new Email(email);
    }

    private static void validateEmail(String email) {
        if (email == null) {
            throw new InvalidArgumentException("이메일은 null 일 수 없습니다.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new InvalidArgumentException("이메일 형식이 아닙니다.");
        }
    }
}
