package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
public class Description {

    @NotNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Description other = (Description) o;
        return Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }
}
