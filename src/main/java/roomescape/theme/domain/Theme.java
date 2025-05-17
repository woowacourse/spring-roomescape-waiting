package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

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

    protected Theme() {
    }

    public Long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public ThemeDescription getDescription() {
        return description;
    }

    public ThemeThumbnail getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
