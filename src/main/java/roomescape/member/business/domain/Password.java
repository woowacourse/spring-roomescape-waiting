package roomescape.member.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    @Column(name = "password")
    private String value;

    public Password() {

    }

    public Password(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
