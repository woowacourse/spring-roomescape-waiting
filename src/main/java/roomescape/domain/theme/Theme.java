package roomescape.domain.theme;

import java.util.Objects;

public class Theme {
    private final long id;
    private final ThemeName name;
    private final String description;
    private final ThumbnailUrl thumbnailUrl;

    private Theme(long id, ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.thumbnailUrl = Objects.requireNonNull(thumbnailUrl);
    }

    public static Theme load(long id, ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    public static Theme create(ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        return new Theme(0L, name, description, thumbnailUrl);
    }

    public Theme withId(long id) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    public long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ThumbnailUrl getThumbnailUrl() {
        return thumbnailUrl;
    }
}
