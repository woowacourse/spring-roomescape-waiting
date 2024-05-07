package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Description {

    @NotBlank
    private String description;

    public Description(String description) {
        this.description = description;
    }

    public Description() {
    }

    public String getDescription() {
        return description;
    }
}
