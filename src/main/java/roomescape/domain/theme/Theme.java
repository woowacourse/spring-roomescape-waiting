package roomescape.domain.theme;

import java.util.Objects;

public class Theme {

    private static final int NAME_MAX_LENGTH = 30;
    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int THUMBNAIL_MAX_LENGTH = 255;

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name, description, thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validate(String name, String description, String thumbnail) {
        validateNAme(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateNAme(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 값입니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("이름은 %d자를 넘을 수 없습니다.", NAME_MAX_LENGTH));
        }
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
