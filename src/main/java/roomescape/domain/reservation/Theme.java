package roomescape.domain.reservation;

import java.util.Objects;

public class Theme {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_THUMBNAIL_URL_LENGTH = 200;

    private final Long id;
    private final ThemeName name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, ThemeName name, String description, String thumbnailUrl) {
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Theme(String name, String description, String thumbnailUrl) {
        this(null, new ThemeName(name), description, thumbnailUrl);
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 설명은 %d자 이하여야 합니다.", MAX_DESCRIPTION_LENGTH));
        }
    }

    private void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl != null && thumbnailUrl.length() > MAX_THUMBNAIL_URL_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 썸네일 URL은 %d자 이하여야 합니다.", MAX_THUMBNAIL_URL_LENGTH));
        }
    }

    public Theme withId(long id) {
        return new Theme(id, name, description, thumbnailUrl);
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
        return name.getName();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
