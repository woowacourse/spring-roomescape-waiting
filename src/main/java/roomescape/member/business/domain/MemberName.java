package roomescape.member.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MemberName {

    @Column(name = "name")
    private String value;

    public MemberName() {

    }

    public MemberName(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
