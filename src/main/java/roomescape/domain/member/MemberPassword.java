package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class MemberPassword {

    private String password;

    public MemberPassword(String password) {
        validatePassword(password);
        this.password = password;
    }

    protected MemberPassword() {
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return !Objects.equals(this.password, other.password);
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 반드시 입력되어야 합니다.");
        }
        if (password.length() > 30) {
            throw new IllegalArgumentException("비밀번호의 길이는 30글자까지 가능합니다.");
        }
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberPassword that = (MemberPassword) o;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(password);
    }
}
