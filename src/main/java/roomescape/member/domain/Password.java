package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record Password(String password) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 16;

    public Password {
        validate(password);
    }

    private void validate(final String password) {
        validateBlank(password);
        validateLength(password);
    }

    private void validateBlank(final String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("값이 존재하지 않습니다.");
        }
    }

    private void validateLength(final String password) {
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 %d자 이상 %d자 이하로 가능합니다.".formatted(
                    MIN_LENGTH, MAX_LENGTH
            ));
        }
    }
}
