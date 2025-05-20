package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.exception.ArgumentNullException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        validateNull(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    private void validateNull(String name, String description, String thumbnail) {
        if (name == null || name.isBlank()) {
            throw new ArgumentNullException("name");
        }
        if (description == null || description.isBlank()) {
            throw new ArgumentNullException("description");
        }
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new ArgumentNullException("thumbnail");
        }
    }

    public static Theme createWithoutId(String name, String description, String thumbnail) {
        return new Theme(null, name, description, thumbnail);
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id) && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description) && Objects.equals(thumbnail,
                theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
