package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Name {

    @NotNull
    @NotBlank
    private String name;

    public Name(String name) {
        this.name = name;
    }

    protected Name() {
    }

    public String getName() {
        return name;
    }
}
