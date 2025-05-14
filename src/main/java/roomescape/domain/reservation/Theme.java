package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.domain.BusinessRuleViolationException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateLength(name, 255, "name");
        validateLength(description, 255, "description");
        validateLength(thumbnail, 255, "thumbnail");
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    public void validateLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new BusinessRuleViolationException(fieldName + "은 " + maxLength + "자를 넘을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id)
                && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
