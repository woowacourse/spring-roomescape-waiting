package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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
    @AttributeOverride(name = "value", column = @Column(name = "theme_name"))
    private ThemeName themeName;

    private String description;

    private String thumbnail;

    public Theme(final Long id, final ThemeName themeName, final String description, final String thumbnail) {
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {

    }

    private void validateDescription(final String description) {
        if (description == null || description.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 설명을 입력해주세요.");
        }
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null || thumbnail.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 썸네일을 입력해주세요.");
        }
    }

    public Theme(final String themeName, final String description, final String thumbnail) {
        this(null, new ThemeName(themeName), description, thumbnail);
    }

    public Long getId() {
        return id;
    }

    public String getThemeNameValue() {
        return themeName.getValue();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
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
        return Objects.equals(id, theme.id) && Objects.equals(themeName, theme.themeName)
                && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, themeName, description, thumbnail);
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", themeName=" + themeName +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
