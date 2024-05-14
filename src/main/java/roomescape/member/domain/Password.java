package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    private static final int MAX_PASSWORD_LENGTH = 20;

    private String value;

    public Password(final String value) {
        validatePassword(value);
        this.value = value;
    }

    public Password() {
    }

    private void validatePassword(final String value) {
        if (value.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("[ERROR] 비밀번호는 " + MAX_PASSWORD_LENGTH + "자 이하로 입력해주세요.");
        }
    }

    public String getValue() {
        return value;
    }
}
