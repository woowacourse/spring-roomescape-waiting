package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    @Column(nullable = false)
    private String password;

    public Password(String password) {
        this.password = password;
    }

    public Password() {

    }

    public String getPassword() {
        return password;
    }
}
