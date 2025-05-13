package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
    @OneToMany(mappedBy = "theme")
    private Set<Reservation> reservations;

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateNameLength(name);
        validateDescriptionLength(description);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    public Theme withId(final long id) {
        if (this.id == null) {
            this.id = id;
            return this;
        }
        throw new IllegalStateException("테마 ID는 재할당할 수 없습니다. 현재 ID: " + this.id);
    }

    private void validateNameLength(final String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("이름은 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private void validateDescriptionLength(final String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("설명은 %d자를 넘길 수 없습니다.", DESCRIPTION_MAX_LENGTH));
        }
    }
}
