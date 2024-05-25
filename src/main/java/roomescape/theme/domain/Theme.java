package roomescape.theme.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false, unique = true, name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "thumbnail")
    private String thumbnail;

    public Theme() {
    }

    public Theme(Long id) {
        this(id, null, null, null);
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
        if (this == o) return true;
        if (!(o instanceof Theme theme)) return false;

        if (id == null || theme.id == null) {
            return Objects.equals(name, theme.name);
        }
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(name);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Theme{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", thumbnail='" + thumbnail + '\'' +
               '}';
    }
}
