package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.domain.exception.BusinessRuleViolationException;

@Embeddable
public record Member(
        @Column(nullable = false)
        String name
) {

    private static final int MAX_NAME_LENGTH = 10;

    public Member(String name) {
        this.name = validateName(name);
    }

    private static String validateName(String input) {
        if (input == null || input.isBlank() || input.strip().length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleViolationException("사용자 이름은 1자 이상 10자 이하여야 합니다.");
        }
        return input.strip();
    }
}
