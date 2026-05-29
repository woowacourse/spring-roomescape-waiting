package roomescape.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Theme {
    private final Long id;
    private final String name;
    private final String thumbnailUrl;
    private final String description;

    public static Theme from(long id, String name, String thumbnailUrl, String description) {
        return new Theme(id, name, thumbnailUrl, description);
    }

    public static Theme create(String name, String thumbnailUrl, String description) {
        return new Theme(null, name, thumbnailUrl, description);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
