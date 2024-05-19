package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    protected Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validate(String name, String description, String thumbnail) {
        validateNullField(name, description, thumbnail);
        validateNotBlank(name, description, thumbnail);
        validateThumbnailFormat(thumbnail);
    }

    private void validateNullField(String name, String description, String thumbnail) {
        if (name == null || description == null || thumbnail == null) {
            throw new IllegalArgumentException("테마 필드에는 빈 값이 들어올 수 없습니다.");
        }
    }

    private void validateNotBlank(String name, String description, String thumbnail) {
        if (name.isBlank() || description.isBlank() || thumbnail.isBlank()) {
            throw new IllegalArgumentException("테마의 정보는 비어있을 수 없습니다.");
        }
    }

    private void validateThumbnailFormat(String thumbnail) {
        if (!thumbnail.matches("(http|https).*")) {
            throw new IllegalArgumentException("썸네일은 url 링크여야합니다.");
        }
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
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
        if (o == null || getClass() != o.getClass()) return false;

        Theme theme = (Theme) o;

        return id.equals(theme.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
