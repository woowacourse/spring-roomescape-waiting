package roomescape.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

@Embeddable
public record UserName(
    @Column(name = "name", nullable = false, length = MAX_LENGTH)
    String value
) {

    private static final int MAX_LENGTH = 5;

    public UserName {
        if (value.contains(" ")) {
            throw new InvalidInputException("이름은 공백을 포함할 수 없습니다.");
        }

        if (value.length() > MAX_LENGTH) {
            var message = String.format("이름은 %d자를 넘길 수 없습니다.", MAX_LENGTH);
            throw new BusinessRuleViolationException(message);
        }
    }
}
