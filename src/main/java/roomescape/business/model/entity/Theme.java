package roomescape.business.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
public class Theme {

    private final Id id;
    private final ThemeName name;
    private final String description;
    private final String thumbnail;

    public static Theme create(final String name, final String description, final String thumbnail) {
        return new Theme(Id.issue(), new ThemeName(name), description, thumbnail);
    }

    public static Theme restore(final String id, final String name, final String description, final String thumbnail) {
        return new Theme(Id.create(id), new ThemeName(name), description, thumbnail);
    }
}
