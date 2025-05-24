package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Entity
public class Theme {

    @EmbeddedId
    private final Id id = Id.issue();
    @Embedded
    private final ThemeName name;
    private final String description;
    private final String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        this.name = new ThemeName(name);
        this.description = description;
        this.thumbnail = thumbnail;
    }
}
