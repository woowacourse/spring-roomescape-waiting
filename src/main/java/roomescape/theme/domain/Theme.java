package roomescape.theme.domain;

import static roomescape.theme.exception.ThemeErrorInformation.DESCRIPTION_IS_NULL;
import static roomescape.theme.exception.ThemeErrorInformation.ID_IS_NULL;
import static roomescape.theme.exception.ThemeErrorInformation.INACTIVE_THEME_NOT_ALLOWED;
import static roomescape.theme.exception.ThemeErrorInformation.NAME_IS_NULL;
import static roomescape.theme.exception.ThemeErrorInformation.THUMBNAIL_URL_IS_NULL;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.theme.exception.ThemeException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Theme {

    public static final String DEFAULT_THUMBNAIL_URL = "DEFAULT_THUMBNAIL_URL";

    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private boolean isActive;

    public static Theme create(String name, String description, String thumbnailUrl) {
        validate(name, description, thumbnailUrl);
        return new Theme(null, name, description, resolveThumbnailUrl(thumbnailUrl), true);
    }

    public static Theme load(Long id, String name, String description, String thumbnailUrl,
        boolean isActive) {
        validateId(id);
        validate(name, description, thumbnailUrl);
        return new Theme(id, name, description, resolveThumbnailUrl(thumbnailUrl), isActive);
    }

    private static void validate(String name, String description, String thumbnailUrl) {
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new ThemeException(ID_IS_NULL);
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ThemeException(NAME_IS_NULL);
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null) {
            throw new ThemeException(THUMBNAIL_URL_IS_NULL);
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ThemeException(DESCRIPTION_IS_NULL);
        }
    }

    private static String resolveThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl.isBlank()) {
            return DEFAULT_THUMBNAIL_URL;
        }
        return thumbnailUrl;
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public void validateIsInactive() {
        if (!isActive) {
            throw new ThemeException(INACTIVE_THEME_NOT_ALLOWED);
        }
    }

}
