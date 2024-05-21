package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
public class Thumbnail {

    @NotNull
    @NotBlank
    private String thumbnail;

    public Thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Thumbnail() {
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Thumbnail other = (Thumbnail) o;
        return Objects.equals(thumbnail, other.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thumbnail);
    }
}
