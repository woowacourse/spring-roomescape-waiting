package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_DESCRIPTION;
import static roomescape.exception.ExceptionType.EMPTY_NAME;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

@Entity
public class Theme extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Embedded
    private Thumbnail thumbnail;
    @OneToMany(mappedBy = "theme")
    private List<Reservation> reservations;

    protected Theme() {
    }

    public Theme(long id, Theme theme) {
        this(id, theme.name, theme.description, theme.getThumbnail());
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = new Thumbnail(thumbnail);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(EMPTY_NAME);
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new RoomescapeException(EMPTY_DESCRIPTION);
        }
    }

    public String getThumbnail() {
        return thumbnail.getUrl();
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public boolean hasIdOf(long id) {
        return this.id == id;
    }

    public boolean hasNameOf(String name) {
        return this.name.equals(name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Theme theme = (Theme) o;

        return Objects.equals(id, theme.id);
    }
}
