package roomescape.domain;

import roomescape.domain.exception.BusinessRuleViolationException;

public record Member(
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
