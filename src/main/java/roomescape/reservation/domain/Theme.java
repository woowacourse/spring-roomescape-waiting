package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "theme_name", nullable = false))
    private ThemeName themeName;

    private String description;

    private String thumbnail;

    protected Theme() {
    }

    public Theme(final Long id, final ThemeName themeName, final String description, final String thumbnail) {
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateDescription(final String description) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 테마 설명을 입력해주세요.");
        }
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
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
}
