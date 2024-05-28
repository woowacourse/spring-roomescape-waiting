package roomescape.domain.theme;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName themeName;

    @Embedded
    private Description description;

    @Embedded
    private Thumbnail thumbnail;

    protected Theme() {
    }

    public Theme(String themeName, String description, String thumbnail) {
        this(null, themeName, description, thumbnail);
    }

    public Theme(Long id, String themeName, String description, String thumbnail) {
        this.id = id;
        this.themeName = new ThemeName(themeName);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    public Long getId() {
        return id;
    }

    public String getThemeName() {
        return themeName.getThemeName();
    }

    public String getDescription() {
        return description.getDescription();
    }

    public String getThumbnail() {
        return thumbnail.getThumbnail();
    }
}
