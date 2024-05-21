package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "theme")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Embedded
    private ThemeName name;
    @Embedded
    private ThemeDescription description;
    @Embedded
    private ThemeThumbnail thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        this(Objects.requireNonNull(id),
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail));
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    private Theme(Long id, ThemeName name, ThemeDescription description, ThemeThumbnail thumbnail) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.thumbnail = Objects.requireNonNull(thumbnail);
    }

    protected Theme() {
    }

    public Theme withId(Long id) {
        return new Theme(id, this.name, this.description, this.thumbnail);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.name();
    }

    public String getDescription() {
        return description.description();
    }

    public String getThumbnail() {
        return thumbnail.thumbnail();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Theme theme = (Theme) o;

        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Theme{" +
               "id=" + id +
               ", name=" + name +
               ", description=" + description +
               ", thumbnail=" + thumbnail +
               '}';
    }
}
