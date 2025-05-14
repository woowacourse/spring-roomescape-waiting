package roomescape.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    private String email;

    public Email(String email) {
        this.email = email;
    }

    public Email() {

    }

    public String getEmail() {
        return email;
    }
}
