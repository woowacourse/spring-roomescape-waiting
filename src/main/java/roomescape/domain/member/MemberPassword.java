package roomescape.domain.member;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MemberPassword {

    private String password;

    public MemberPassword(String password) {
        this.password = password;
    }

    protected MemberPassword() {
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return !Objects.equals(this.password, other.password);
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
