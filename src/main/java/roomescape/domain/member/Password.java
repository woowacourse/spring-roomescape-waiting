package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    private static final int MAX_LENGTH = 255;

    @Column(nullable = false)
    private String password;

    protected Password() {
    }

    protected Password(String password) {
        validateBlank(password);
        validateLength(password);
        validateContainsBlank(password);
        this.password = password;
    }

    private void validateBlank(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 값입니다.");
        }
    }

    private void validateLength(String password) {
        if (MAX_LENGTH < password.length()) {
            throw new IllegalArgumentException(String.format("비밀번호는 %d자를 넘을 수 없습니다.", MAX_LENGTH));
        }
    }

    private void validateContainsBlank(String password) {
        if (password.contains(" ")) {
            throw new IllegalArgumentException("비밀번호에 공백이 포함되어 있습니다.");
        }
    }

    protected String getValue() {
        return password;
    }
}
