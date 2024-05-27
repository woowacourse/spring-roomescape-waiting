package roomescape.theme.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.common.model.BaseEntity;

@Entity
public class Theme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateThemeNameIsBlank(name);
        validateThemeDescriptionIsBlank(description);
        validateThemeThumbnailIsBlank(thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    private void validateThemeNameIsBlank(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 생성 시 테마 명은 필수입니다.");
        }
    }

    private void validateThemeDescriptionIsBlank(final String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 생성 시 테마 설명은 필수입니다.");
        }
    }

    private void validateThemeThumbnailIsBlank(final String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new IllegalArgumentException("테마 생성 시 썸네일은 필수입니다.");
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
