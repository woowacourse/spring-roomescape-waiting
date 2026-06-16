package roomescape.theme.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.theme.exception.ThemeException;

import static roomescape.theme.exception.ThemeErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Theme {

    public static final String DEFAULT_THUMBNAIL_URL = "DEFAULT_THUMBNAIL_URL";

    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    private boolean isActive;
    private Long amount;

    public static Theme create(String name, String description, String thumbnailUrl, Long amount) {
        validate(name, description, thumbnailUrl, amount);
        return new Theme(null, name, description, resolveThumbnailUrl(thumbnailUrl), true, amount);
    }

    public static Theme load(Long id, String name, String description, String thumbnailUrl, boolean isActive, Long amount) {
        validateId(id);
        validate(name, description, thumbnailUrl, amount);
        return new Theme(id, name, description, resolveThumbnailUrl(thumbnailUrl), isActive, amount);
    }

    private static void validate(String name, String description, String thumbnailUrl, Long amount) {
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        validateAmount(amount);
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

    private static void validateAmount(Long amount) {
        if (amount == null) {
            throw new ThemeException(AMOUNT_IS_NULL);
        }
        if (amount < 0) {
            throw new ThemeException(AMOUNT_IS_NEGATIVE);
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
