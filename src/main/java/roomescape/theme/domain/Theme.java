package roomescape.theme.domain;

public class Theme {
    private final Long id;
    private final ThemeName name;
    private final Description description;
    private final Thumbnail thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final Theme theme) {
        this(id, theme.name, theme.description, theme.thumbnail);
    }

    public Theme(final Long id, final String name, String description, final String thumbnail) {
        this(id, new ThemeName(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Theme(final Long id, final ThemeName name, final Description description, final Thumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", name=" + name +
                ", description=" + description +
                ", thumbnail=" + thumbnail +
                '}';
    }
}
