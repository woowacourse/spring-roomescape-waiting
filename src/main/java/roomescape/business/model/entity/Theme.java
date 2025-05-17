package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class Theme {

    @EmbeddedId
    private final Id id;
    @Embedded
    private ThemeName name;
    private String description;
    private String thumbnail;

    protected Theme() {
        id = Id.issue();
    }

    public static Theme create(final String name, final String description, final String thumbnail) {
        return new Theme(Id.issue(), new ThemeName(name), description, thumbnail);
    }

    public static Theme restore(final String id, final String name, final String description, final String thumbnail) {
        return new Theme(Id.create(id), new ThemeName(name), description, thumbnail);
    }
}
