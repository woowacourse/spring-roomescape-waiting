package roomescape.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Password {
    private String password;
    private String salt;

    protected Password() {
    }

    public Password(final String password, final String salt) {
        this.password = password;
        this.salt = salt;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public boolean check(final Password other) {
        return password.equals(other.password) && salt.equals(other.salt);
    }
}
