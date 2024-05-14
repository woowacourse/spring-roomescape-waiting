package roomescape.domain.member;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class MemberPassword {

    private String value;

    public MemberPassword(String value) {
        this.value = value;
    }

    protected MemberPassword() {
    }

    public boolean isMismatchedPassword(MemberPassword other) {
        return !Objects.equals(this.value, other.value);
    }

    public String getValue() {
        return value;
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
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
