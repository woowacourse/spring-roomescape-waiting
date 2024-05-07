package roomescape.model.member;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Name {

    @NotBlank
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
