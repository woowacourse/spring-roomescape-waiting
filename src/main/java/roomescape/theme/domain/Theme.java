package roomescape.theme.domain;

import jakarta.persistence.*;
import roomescape.exception.BadRequestException;

@Entity
public class Theme {

    public static final long POPULAR_THEME_PERIOD = 7L;
    public static final long POPULAR_THEME_COUNT = 10L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Embedded
    private ThemeThumbnail thumbnail;

    protected Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = new ThemeThumbnail(thumbnail);
    }

    private void validate(String name, String description, String thumbnail) {
        validateNullField(name, description, thumbnail);
        validateNotBlank(name, description, thumbnail);
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

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public void validateDuplicatedName(Theme theme) {
        if (name.equals(theme.name)) {
            throw new BadRequestException("중복된 테마 이름입니다.");
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
        return thumbnail.getThumbnail();
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
