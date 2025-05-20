package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.InvalidInputException;

@Embeddable
public record ThemeName(
    @Column(name = "name", nullable = false, length = MAX_LENGTH)
    String value
) {

    private static final int MAX_LENGTH = 10;

    public ThemeName {
        if (value.isBlank()) {
            throw new InvalidInputException("이름은 공백일 수 없습니다.");
        }

        if (value.length() > MAX_LENGTH) {
            throw new BusinessRuleViolationException(String.format("이름은 %d자를 넘길 수 없습니다.", MAX_LENGTH));
        }
    }
}
