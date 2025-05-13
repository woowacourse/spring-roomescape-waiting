package roomescape.theme.domain;

import java.util.Objects;

public class Theme {

    private static int MAX_NAME = 255;
    private static int MAX_DESCRIPTION = 255;
    private static int MAX_THUMBNAIL = 255;

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    private Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme createWithoutId(final String name, final String description, final String thumbnail) {
        validate(name, description, thumbnail);
        return new Theme(null, name, description, thumbnail);
    }

    public static Theme createWithId(final Long id, final String name, final String description,
                                     final String thumbnail) {
        validate(name, description, thumbnail);
        return new Theme(Objects.requireNonNull(id), name, description, thumbnail);
    }

    private static void validate(final String name, final String description, final String thumbnail) {
        if (name == null || name.isBlank() || name.length() > MAX_NAME) {
            throw new IllegalArgumentException("이름은 1글자 이상, 255글자 이하여야합니다.");
        }
        if (description == null || description.isBlank() || description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException("설명은 1글자 이상, 255글자 이하여야합니다.");
        }
        if (thumbnail == null || thumbnail.isBlank() || thumbnail.length() > MAX_THUMBNAIL) {
            throw new IllegalArgumentException("썸네일 URI는 1글자 이상, 255글자 이하여야합니다.");
        }
    }

    public Theme assignId(final Long id) {
        return new Theme(Objects.requireNonNull(id), name, description, thumbnail);
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
    public boolean equals(final Object object) {
        if (!(object instanceof Theme theme)) {
            return false;
        }
        return Objects.equals(getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
