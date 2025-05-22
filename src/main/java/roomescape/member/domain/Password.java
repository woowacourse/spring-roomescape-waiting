package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.common.exception.BusinessException;
import roomescape.member.exception.PasswordException;

@Embeddable
public record Password(@Column(length = MAX_PASSWORD_LENGTH, nullable = false) String password) {

    public static final int MAX_PASSWORD_LENGTH = 20;

    public Password {
        validatePasswordIsNonEmpty(password);
        validatePasswordMaxLength(password);
    }

    private void validatePasswordIsNonEmpty(final String password) {
        if (password == null || password.isEmpty()) {
            throw new PasswordException("비밀번호는 비어있을 수 없습니다.");
        }
    }

    private void validatePasswordMaxLength(final String password) {
        if (password.length() >= MAX_PASSWORD_LENGTH) {
            throw new BusinessException(String.format("비밀번호는 %d자를 넘을 수 없습니다.", MAX_PASSWORD_LENGTH));
        }
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Password password1)) {
            return false;
        }
        return Objects.equals(getPassword(), password1.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPassword());
    }
}
