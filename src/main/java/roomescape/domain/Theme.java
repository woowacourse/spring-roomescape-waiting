package roomescape.domain;

import java.util.Objects;

public class Theme {

    private Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme createWithId(Long id) {
        return new Theme(id, this.name, this.description, this.thumbnail);
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
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Theme theme = (Theme) object;
        if (id != null && theme.id != null) {
            return Objects.equals(id, theme.id);
        }
        return Objects.equals(name, theme.name) && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, description, thumbnail);
    }
}
