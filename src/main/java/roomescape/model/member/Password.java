package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Embeddable
public class Password {

    @NotBlank
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public Password() {
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
        Password other = (Password) o;
        return Objects.equals(password, other.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password);
    }
}
