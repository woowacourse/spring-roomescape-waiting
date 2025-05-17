package roomescape.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

@Embeddable
public record Password(
    @Column(name = "password", nullable = false, length = 30)
    String value
) {

    private static final int MAX_LENGTH = 30;

    public Password {
        if (value.contains(" ")) {
            throw new InvalidInputException("비밀번호는 공백을 포함할 수 없습니다.");
        }

        if (value.length() > MAX_LENGTH) {
            var message = String.format("비밀번호는 %d자를 넘길 수 없습니다.", MAX_LENGTH);
            throw new BusinessRuleViolationException(message);
        }
    }

    public boolean matches(Password password) {
        return this.equals(password);
    }
}
