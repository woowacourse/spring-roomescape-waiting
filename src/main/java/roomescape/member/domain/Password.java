package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class Password {

    private String password;

    public Password(final String password) {
        this.password = password;
    }

    protected Password() {

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
