package roomescape.domain;

import lombok.Getter;

@Getter
public class Theme {

    private static final int DESCRIPTION_MINIMUM_LENGTH = 5;

    private final Long id;
    private final ThemeName name;
    private final String description;
    private final String thumbnailUrl;
    private final Long price;

    private Theme(final Long id, final ThemeName name, final String description, final String thumbnailUrl, final Long price) {
        this.id = id;
        this.name = name;
        validateDescription(description);
        this.description = description;
        validateThumbnailUrl(thumbnailUrl);
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("테마 ID는 비워둘 수 없습니다.");
        }
    }

    private void validateDescription(final String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 비워둘 수 없습니다.");
        }

        if (description.length() < DESCRIPTION_MINIMUM_LENGTH) {
            throw new IllegalArgumentException("테마 설명은 최소 5자 이상이어야 합니다.");
        }
    }

    private void validateThumbnailUrl(final String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new IllegalArgumentException("테마 썸네일 URL은 비워둘 수 없습니다.");
        }
    }

    public static Theme create(final String name, final String description, final String thumbnailUrl, final Long price) {
        return new Theme(
                null,
                new ThemeName(name),
                description,
                thumbnailUrl,
                price
        );
    }

    public static Theme createWithId(
            final Long id,
            final String name,
            final String description,
            final String thumbnailUrl,
            final Long price
    ) {
        validateId(id);
        return new Theme(
                id,
                new ThemeName(name),
                description,
                thumbnailUrl,
                price
        );
    }

    public Theme withId(final Long id) {
        validateId(id);
        return new Theme(
                id,
                this.name,
                this.description,
                this.thumbnailUrl,
                this.price
        );
    }

    public String getName() {
        return this.name.name();
    }
}
