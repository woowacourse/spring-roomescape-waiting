package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ThemeName name;
    @Embedded
    private ThemeDescription description;
    private String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = new ThemeName(name);
        this.description = new ThemeDescription(description);
        this.thumbnail = thumbnail;
    }

    public Theme() {
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null) {
            throw new IllegalArgumentException("썸네일을 입력해야 합니다.");
        }
    }

    public boolean hasSameId(final long other) {
        return id == other;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public ThemeName getThemeName() {
        return name;
    }

    public String getDescription() {
        return description.getDescription();
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
