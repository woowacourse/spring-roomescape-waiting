package roomescape.model.theme;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class Thumbnail {

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
