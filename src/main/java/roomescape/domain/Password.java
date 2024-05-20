package roomescape.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Password {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private String value;

    protected Password() {
    }

    public Password(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String password) {
        validateBlank(password);
        validateLength(password);
    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수로 입력해야 합니다.");
        }
    }

    private void validateLength(String name) {
        if (name.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(String.format("비밀번호는 %s 이상이어야 합니다.", MIN_PASSWORD_LENGTH));
        }
    }

    public String getValue() {
        return value;
    }
}
