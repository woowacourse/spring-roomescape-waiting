package roomescape.domain;

import java.util.Objects;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    private Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme of(Long id, String name, String description, String thumbnail) {
        return new Theme(id, name, description, thumbnail);
    }

    public static Theme withoutId(String name, String description, String thumbnail) {
        return new Theme(null, name, description, thumbnail);
    }

    public static Theme assignId(Long id, Theme themeWithoutId) {
        return new Theme(id, themeWithoutId.name, themeWithoutId.description, themeWithoutId.thumbnail);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Theme theme)) {
            return false;
        }
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
