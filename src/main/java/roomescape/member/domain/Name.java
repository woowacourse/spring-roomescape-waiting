package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Name {

    @Column(nullable = false)
    private String name;

    public Name(String name) {
        this.name = name;
    }

    public Name() {

    }

    public String getName() {
        return name;
    }
}
