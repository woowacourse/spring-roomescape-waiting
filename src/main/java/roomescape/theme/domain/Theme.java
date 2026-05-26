package roomescape.theme.domain;

import roomescape.common.exception.DomainException;

import java.time.LocalDateTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonBlank;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
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
        validateTheme(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.deletedAt = deletedAt;
    }

    public Theme withId(Long id) {
        requireNonNull(id, new DomainException(INVALID_THEME_ID));
        require(this.id == null, new DomainException(THEME_ALREADY_HAS_ID));

        return new Theme(id, name, description, thumbnail, deletedAt);
    }

    private void validateTheme(String name, String description, String thumbnail) {
        requireNonBlank(name, new DomainException(INVALID_THEME_NAME));
        requireNonBlank(description, new DomainException(INVALID_THEME_DESCRIPTION));
        requireNonBlank(thumbnail, new DomainException(INVALID_THEME_THUMBNAIL));
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
