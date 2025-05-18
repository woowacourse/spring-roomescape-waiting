package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

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
