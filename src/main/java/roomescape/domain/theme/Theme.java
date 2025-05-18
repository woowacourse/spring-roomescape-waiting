package roomescape.domain.theme;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.exception.BusinessRuleViolationException;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity
public class Theme {

    private static final int NAME_MAX_LENGTH = 10;
    private static final int DESCRIPTION_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateNameLength(name);
        validateDescriptionLength(description);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    protected Theme() {
    }

    private void validateNameLength(final String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessRuleViolationException(String.format("이름은 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private void validateDescriptionLength(final String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessRuleViolationException(String.format("설명은 %d자를 넘길 수 없습니다.", DESCRIPTION_MAX_LENGTH));
        }
    }
}
