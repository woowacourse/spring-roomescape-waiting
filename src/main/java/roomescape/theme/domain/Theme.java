package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import roomescape.theme.exception.InvalidThemeException;

@Entity
@Table(name = "themes")
public class Theme {

    private static final int MAX_NAME_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 100;
    private static final int MAX_THUMBNAIL_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        validateNameLength(name);
        validateDescriptionLength(description);
        validateThumbnailLength(thumbnail);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    private void validateNameLength(final String value) {
        if (value.length() > MAX_NAME_LENGTH) {
            throw new InvalidThemeException("이름은 10글자 이내여야 합니다.");
        }
    }

    private void validateDescriptionLength(final String value) {
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidThemeException("설명은 100글자 이내여야 합니다.");
        }
    }

    private void validateThumbnailLength(final String value) {
        if (value.length() > MAX_THUMBNAIL_LENGTH) {
            throw new InvalidThemeException("썸네일은 100글자 이내여야 합니다.");
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Theme theme)) {
            return false;
        }
        return Objects.equals(getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
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
}
