package roomescape.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    public static final int NAME_MAX_LENGTH = 15;
    public static final int DESCRIPTION_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    protected Theme() {
    }

    private Theme(Long id, String name, String description, String thumbnail) {
        validateThemeInfo(name, description, thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme withId(Long id, String name, String description, String thumbnail) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요.");
        }

        return new Theme(id, name, description, thumbnail);
    }

    public static Theme withoutId(String name, String description, String thumbnail) {
        return new Theme(null, name, description, thumbnail);
    }

    private void validateThemeInfo(String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 공백일 수 없습니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            String message = String.format("테마 이름은 %d자를 넘길 수 없습니다.", NAME_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 공백일 수 없습니다.");
        }

        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            String message = String.format("테마 설명은 %d자를 넘길 수 없습니다.", DESCRIPTION_MAX_LENGTH);
            throw new IllegalArgumentException(message);
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank() || thumbnail.contains(" ")) {
            throw new IllegalArgumentException("썸네일 url은 공백이거나 공백을 포함할 수 없습니다.");
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Theme that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
