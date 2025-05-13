package roomescape.theme.domain;

public final class Theme {

    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeThumbnail thumbnail;

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
