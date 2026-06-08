package roomescape.domain;

import java.util.Objects;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("name은 255자를 넘을 수 없습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("description은 255자를 넘을 수 없습니다.");
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail != null && thumbnail.length() > 255) {
            throw new IllegalArgumentException("thumbnail은 255자를 넘을 수 없습니다.");
        }
    }

    public Theme withId(Long id) {
        return new Theme(id, name, description, thumbnail);
    }
}
