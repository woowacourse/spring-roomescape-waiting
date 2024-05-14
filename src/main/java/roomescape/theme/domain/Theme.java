package roomescape.theme.domain;

import java.util.Objects;

public class Theme {
    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeThumbnail thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        this(Objects.requireNonNull(id),
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail));
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    private Theme(Long id, ThemeName name, ThemeDescription description, ThemeThumbnail thumbnail) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.thumbnail = Objects.requireNonNull(thumbnail);
    }

    public Theme withId(Long id) {
        return new Theme(id, this.name, this.description, this.thumbnail);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getDescription() {
        return description.value();
    }

    public String getThumbnail() {
        return thumbnail.value();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
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
