package roomescape.member.domain;

import roomescape.exceptions.ValidationException;

public record Password(String password) {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final String INVALID_PASSWORD_LENGTH = String.format(
            "패스워드는 %d자 이상 %d자 이하여야 합니다.", MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH
    );

    public Password {
        validateLength(password);
    }

    private void validateLength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException(INVALID_PASSWORD_LENGTH);
        }
    }
}
