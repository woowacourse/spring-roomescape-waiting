package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    @Column(nullable = false)
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
