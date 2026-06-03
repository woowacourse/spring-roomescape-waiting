package roomescape.domain;

import lombok.Getter;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class Theme {

    private static final int DESCRIPTION_MINIMUM_LENGTH = 5;

    private final Long id;
    private final ThemeName name;
    private final String description;
    private final String thumbnailUrl;

    private Theme(final Long id, final ThemeName name, final String description, final String thumbnailUrl) {
        this.id = id;
        this.name = name;
        validateDescription(description);
        this.description = description;
        validateThumbnailUrl(thumbnailUrl);
        this.thumbnailUrl = thumbnailUrl;
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.THEME_ID_NULL);
        }
    }

    private void validateDescription(final String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException(ErrorCode.DESCRIPTION_NULL_OR_BLANK);
        }

        if (description.length() < DESCRIPTION_MINIMUM_LENGTH) {
            throw new BusinessException(ErrorCode.DESCRIPTION_TOO_SHORT);
        }
    }

    private void validateThumbnailUrl(final String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new BusinessException(ErrorCode.THUMBNAIL_URL_NULL_OR_BLANK);
        }
    }

    public static Theme create(final String name, final String description, final String thumbnailUrl) {
        return new Theme(
                null,
                new ThemeName(name),
                description,
                thumbnailUrl
        );
    }

    public static Theme createWithId(
            final Long id,
            final String name,
            final String description,
            final String thumbnailUrl
    ) {
        validateId(id);
        return new Theme(
                id,
                new ThemeName(name),
                description,
                thumbnailUrl
        );
    }

    public Theme withId(final Long id) {
        validateId(id);
        return new Theme(
                id,
                this.name,
                this.description,
                this.thumbnailUrl
        );
    }

    public String getName() {
        return this.name.name();
    }
}
