package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import roomescape.member.exception.PasswordException;

@Embeddable
public record Password(String password) {

    public Password {
        validatePasswordIsNonEmpty(password);
    }

    private static void validatePasswordIsNonEmpty(final String password) {
        if (password == null || password.isEmpty()) {
            throw new PasswordException("비밀번호는 비어있을 수 없습니다.");
        }
    }
}
