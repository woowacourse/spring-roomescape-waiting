package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.InvalidMemberException;

@Embeddable
public class Password {
    private static final int MINIMUM_PASSWORD_LENGTH = 6;
    private static final int MAXIMUM_PASSWORD_LENGTH = 12;

    private String value;

    protected Password() {
    }

    private Password(String value) {
        validate(value);
        this.value = value;
    }

    public static Password of(String password) {
        return new Password(password);
    }

    private void validate(String value) {
        if (value.length() < MINIMUM_PASSWORD_LENGTH || value.length() > MAXIMUM_PASSWORD_LENGTH) {
            throw new InvalidMemberException("비밀번호는 6자 이상 12자 이하여야 합니다.");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    public String getValue() {
        return value;
    }
}
