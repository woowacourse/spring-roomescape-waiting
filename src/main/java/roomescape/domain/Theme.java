package roomescape.domain;

import java.util.Objects;
import roomescape.domain.vo.Name;

public class Theme {
    private final Long id;
    private final Name name;
    private final String thumbnailUrl;
    private final String description;

    public Theme(Long id, Name name, String thumbnailUrl, String description) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    public Theme(Name name, String thumbnailUrl, String description) {
        this(null, name, thumbnailUrl, description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(thumbnailUrl);
        result = 31 * result + Objects.hashCode(description);
        return result;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Theme theme)) {
            return false;
        }

        return Objects.equals(id, theme.id) && Objects.equals(name, theme.name)
                && Objects.equals(thumbnailUrl, theme.thumbnailUrl) && Objects.equals(description,
                theme.description);
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDescription() {
        return description;
    }
}
