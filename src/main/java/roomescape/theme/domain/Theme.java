package roomescape.theme.domain;

import lombok.Getter;

@Getter
public class Theme {

    private final Long id;
    private final ThemeName name;
    private final String description;
    private final String thumbnailUrl;
    private final int price;

    private Theme(
            final Long id,
            final ThemeName name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        validatePrice(price);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public static Theme create(final String name, final String description, final String thumbnailUrl, final int price) {
        return new Theme(
                null,
                ThemeName.from(name),
                description,
                thumbnailUrl,
                price
        );
    }

    public static Theme of(
            final Long id,
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        return new Theme(
                id,
                ThemeName.from(name),
                description,
                thumbnailUrl,
                price
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

    private void validatePrice(final int price) {
        if (price <= 0) {
            throw new IllegalArgumentException("테마 가격은 양수여야 합니다.");
        }
    }
}
