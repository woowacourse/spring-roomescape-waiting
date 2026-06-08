package roomescape.domain.theme;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.DomainPreconditions;

import java.util.Objects;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.requireNonBlank;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class Theme {
    private final Long id;
    private final ThemeName name;
    private final String description;
    private final ThumbnailUrl thumbnailUrl;

    private Theme(Long id, ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        this.id = id;
        this.name = requireNonNull(name, INVALID_INPUT, "테마 이름은 비어있을 수 없습니다.");
        this.description = requireNonBlank(description, INVALID_INPUT, "테마 설명은 비어있을 수 없습니다.");
        this.thumbnailUrl = requireNonNull(thumbnailUrl, INVALID_INPUT, "테마 섬네일 URL은 비어있을 수 없습니다.");
    }

    public static Theme load(Long id, String name, String description, String thumbnailUrl) {
        return new Theme(id, new ThemeName(name), description, new ThumbnailUrl(thumbnailUrl));
    }

    public static Theme create(ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public Theme withId(Long generatedKey) {
        return new Theme(generatedKey, name, description, thumbnailUrl);
    }

    public Long getId() {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
