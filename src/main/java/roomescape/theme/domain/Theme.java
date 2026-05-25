package roomescape.theme.domain;

import java.util.Objects;
import roomescape.theme.exception.InvalidThemeRequestValueException;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        validate(name, description, thumbnailUrl);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Theme of(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    private void validate(String name, String description, String thumbnailUrl) {
        if (name.isBlank() || description.isBlank() || thumbnailUrl.isBlank()) {
            throw new InvalidThemeRequestValueException();
        }
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Theme theme = (Theme) o;
        return Objects.equals(getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
