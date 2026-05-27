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
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.THEME_NAME_BLANK);
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new CustomException(ErrorCode.THEME_DESCRIPTION_BLANK);
        }
    }

    private void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new CustomException(ErrorCode.THEME_THUMBNAIL_URL_BLANK);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id)
                && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description)
                && Objects.equals(thumbnailUrl, theme.thumbnailUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnailUrl);
    }
}
