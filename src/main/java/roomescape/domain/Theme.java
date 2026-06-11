package roomescape.domain;

import java.util.Objects;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        validateNameLength(name);
        validateDescriptionLength(description);
        validateThumbnailUrlLength(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    private void validateNameLength(String name) {
        if (name.length() > 255) {
            throw new CustomException(ErrorCode.THEME_NAME_TOO_LONG);
        }
    }

    private void validateDescriptionLength(String description) {
        if (description.length() > 255) {
            throw new CustomException(ErrorCode.THEME_DESCRIPTION_TOO_LONG);
        }
    }

    private void validateThumbnailUrlLength(String thumbnailUrl) {
        if (thumbnailUrl.length() > 255) {
            throw new CustomException(ErrorCode.THEME_THUMBNAIL_TOO_LONG);
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(name, theme.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
