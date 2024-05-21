package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Description {

    @NotNull
    @NotBlank
    private String description;

    public Description(String description) {
        this.description = description;
    }

    protected Description() {
    }

    public String getDescription() {
        return description;
    }
}
