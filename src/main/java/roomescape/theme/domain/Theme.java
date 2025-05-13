package roomescape.theme.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.common.exception.ThemeException;

@Getter
@EqualsAndHashCode(of = {"id"})
public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new ThemeException("Name cannot be null or blank");
        }
    }

    private void validateDescription(final String description) {
        if (description == null || description.isBlank()) {
            throw new ThemeException("Description cannot be null or blank");
        }
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new ThemeException("Thumbnail cannot be null or blank");
        }
    }
}
