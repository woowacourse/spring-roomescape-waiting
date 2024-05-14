package roomescape.reservation.model;

import java.util.Objects;

public class Theme {

    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeThumbnail thumbnail;

    public static Theme of(final String name, final String description, final String thumbnail) {
        return new Theme(null, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    public static Theme of(final Long id, final String name, final String description, final String thumbnail) {
        return new Theme(id, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    private Theme(final Long id, final ThemeName name, final ThemeDescription description, final ThemeThumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme initializeIndex(final Long themeId) {
        return new Theme(themeId, name, description, thumbnail);
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
        if (this == o) return true;
        if (!(o instanceof final Theme theme)) return false;
        return Objects.equals(id, theme.id)
                && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
