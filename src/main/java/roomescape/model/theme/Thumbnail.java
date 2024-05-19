package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
}
