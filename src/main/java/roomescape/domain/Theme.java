package roomescape.domain;

import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.domain.vo.Name;

public class Theme {
    private final Long id;
    private final Name name;
    private final String thumbnailUrl;
    private final String description;

    public Theme(Long id, Name name, String thumbnailUrl, String description) {
        DomainAssert.notNull(name, "테마 이름은 비어 있을 수 없습니다.");
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
        return Objects.hashCode(id);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Theme theme)) {
            return false;
        }

        return id != null && Objects.equals(id, theme.id);
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
