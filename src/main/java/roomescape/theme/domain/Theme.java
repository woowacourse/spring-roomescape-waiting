package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public final class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ThemeName name;
    private ThemeDescription description;
    private ThemeThumbnail thumbnail;

    public Theme(final Long id, final ThemeName name,
                 final ThemeDescription description, final ThemeThumbnail thumbnail
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this(id, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    public Theme() {

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public ThemeName getThemeName() {
        return name;
    }

    public String getDescription() {
        return description.getDescription();
    }

    public String getThumbnail() {
        return thumbnail.getThumbnail();
    }
}
