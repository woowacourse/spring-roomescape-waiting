package roomescape.domain.theme;

import java.util.Objects;
import roomescape.common.DomainAssert;
import roomescape.domain.vo.Name;

public class Theme {
    private final Long id;
    private final Name name;
    private final String thumbnailUrl;
    private final String description;
    private final long price;

    public Theme(Long id, Name name, String thumbnailUrl, String description, long price) {
        DomainAssert.notNull(name, "테마 이름은 비어 있을 수 없습니다.");
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.price = price;
    }

    public Theme(Name name, String thumbnailUrl, String description, long price) {
        this(null, name, thumbnailUrl, description, price);
    }

    public Theme(Long id, Name name, String thumbnailUrl, String description) {
        this(id, name, thumbnailUrl, description, 0L);
    }

    public Theme(Name name, String thumbnailUrl, String description) {
        this(null, name, thumbnailUrl, description, 0L);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
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

    public long getPrice() {
        return price;
    }
}
