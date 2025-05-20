package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.ThemeException;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

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
