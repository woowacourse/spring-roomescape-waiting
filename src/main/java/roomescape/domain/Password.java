package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {
    @Column(nullable = false, name = "password")
    private String password;
    @Column(nullable = false, name = "salt")
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
