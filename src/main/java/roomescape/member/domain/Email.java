package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    @Column(name = "email")
    private String value;

    public Email() {

    }

    public Email(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
