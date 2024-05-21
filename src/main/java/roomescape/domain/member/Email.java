package roomescape.domain.member;

import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    private String email;

    protected Email() {
    }

    public Email(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
