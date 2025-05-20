package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.BusinessRuleViolationException;

@Embeddable
public record Description(
    @Column(name = "description", nullable = false, length = MAX_LENGTH)
    String value
) {

    private static final int MAX_LENGTH = 50;

    public Description {
        if (value.length() > MAX_LENGTH) {
            throw new BusinessRuleViolationException(
                String.format("설명은 %d자를 넘길 수 없습니다.", MAX_LENGTH));
        }
    }
}
