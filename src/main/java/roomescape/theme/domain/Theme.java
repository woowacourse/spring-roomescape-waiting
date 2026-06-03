package roomescape.theme.domain;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonBlank;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME_DESCRIPTION;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME_NAME;
import static roomescape.theme.exception.ThemeErrorCode.INVALID_THEME_THUMBNAIL;
import static roomescape.theme.exception.ThemeErrorCode.THEME_ALREADY_HAS_ID;

import java.util.Objects;
import lombok.Getter;
import roomescape.common.exception.DomainException;

@Getter
public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    private Theme(
            Long id,
            String name,
            String description,
            String thumbnail
    ) {
        validateTheme(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme create(String name, String description, String thumbnail) {
        return new Theme(null, name, description, thumbnail);
    }

    public static Theme of(long id, String name, String description, String thumbnail) {
        return new Theme(id, name, description, thumbnail);
    }

    public Theme withId(long id) {
        require(this.id == null, new DomainException(THEME_ALREADY_HAS_ID));
        return of(id, name, description, thumbnail);
    }

    private void validateTheme(String name, String description, String thumbnail) {
        requireNonBlank(name, new DomainException(INVALID_THEME_NAME));
        requireNonBlank(description, new DomainException(INVALID_THEME_DESCRIPTION));
        requireNonBlank(thumbnail, new DomainException(INVALID_THEME_THUMBNAIL));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Theme theme)) {
            return false;
        }
        return id != null && Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
