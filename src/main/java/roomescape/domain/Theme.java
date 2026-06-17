package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String thumbnail;

    protected Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        Objects.requireNonNull(name, "테마 이름은 필수값 입니다.");
        Objects.requireNonNull(description, "테마 설명은 필수값 입니다.");
        Objects.requireNonNull(thumbnail, "테마 썸네일은 필수값 입니다.");
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Theme theme = (Theme) object;
        if (id != null && theme.id != null) {
            return Objects.equals(id, theme.id);
        }
        return Objects.equals(name, theme.name) && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, description, thumbnail);
    }
}
