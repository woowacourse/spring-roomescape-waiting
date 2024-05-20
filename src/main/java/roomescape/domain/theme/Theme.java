package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int THUMBNAIL_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    protected Theme() {
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateDescription(description);
        validateThumbnail(thumbnail);

        this.id = id;
        this.name = new ThemeName(name);
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("설명은 필수 값입니다.");
        }

        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("설명은 %d자를 넘을 수 없습니다.", DESCRIPTION_MAX_LENGTH));
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new IllegalArgumentException("이미지는 필수 값입니다.");
        }

        if (thumbnail.length() > THUMBNAIL_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("이미지는 %d자를 넘을 수 없습니다.", THUMBNAIL_MAX_LENGTH));
        }
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public String getRawName() {
        return name.getValue();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
