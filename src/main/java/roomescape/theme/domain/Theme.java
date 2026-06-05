package roomescape.theme.domain;

import lombok.Getter;

@Getter
public class Theme {

    private final Long id;
    private final ThemeName name;
    private final String description;
    private final String thumbnailUrl;

    private Theme(final Long id, final ThemeName name, final String description, final String thumbnailUrl) {
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Theme create(final String name, final String description, final String thumbnailUrl) {
        return new Theme(
                null,
                ThemeName.from(name),
                description,
                thumbnailUrl
        );
    }

    public static Theme of(
            final Long id,
            final String name,
            final String description,
            final String thumbnailUrl
    ) {
        return new Theme(
                id,
                ThemeName.from(name),
                description,
                thumbnailUrl
        );
    }

    public String getName() {
        return name.getName();
    }

    private void validateDescription(final String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명을 입력해야 합니다.");
        }
    }

    private void validateThumbnailUrl(final String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new IllegalArgumentException("썸네일 URL을 입력해야 합니다.");
        }
    }
}
