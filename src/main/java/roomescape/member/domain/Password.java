package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;

@Embeddable
public final class Password {

    private String password;

    public Password(final String password) {
        validatePassword(password);
        this.password = password;
    }

    protected Password() {

    }

    private void validatePassword(final String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("비밀번호를 입력해 주세요.");
        }
    }

    public String getValue() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Password password1 = (Password) o;
        return Objects.equals(password, password1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(password);
    }
}
