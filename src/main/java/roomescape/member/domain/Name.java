package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Name {

    @Column(nullable = false)
    private String name;

    public Name(String name) {
        validateBlank(name);
        this.name = name;
    }

    protected Name() {

    }

    private void validateBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 이름은 공백이 될 수 없습니다.");
        }
    }

    public String getName() {
        return name;
    }
}
