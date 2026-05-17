package roomescape.theme.domain;

import roomescape.common.exception.DomainException;

import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.theme.exception.ThemeErrorCode.*;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;
    private final LocalDateTime deletedAt;

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        this(id, name, description, thumbnail, null);
    }

    public Theme(Long id, String name, String description, String thumbnail, LocalDateTime deletedAt) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.deletedAt = deletedAt;
    }

    public Theme withId(Long id) {
        validateId(id);
        if (this.id != null) {
            throw new DomainException(THEME_ALREADY_HAS_ID);
        }

        return new Theme(id, name, description, thumbnail, deletedAt);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new DomainException(INVALID_THEME_ID);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException(INVALID_THEME_NAME);
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainException(INVALID_THEME_DESCRIPTION);
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new DomainException(INVALID_THEME_THUMBNAIL);
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

    public String getThumbnail() {
        return thumbnail;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme theme)) return false;
        return id != null && Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
