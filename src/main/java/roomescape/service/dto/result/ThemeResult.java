package roomescape.service.dto.result;

import roomescape.domain.Theme;

public record ThemeResult(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        Long price
) {

    public static ThemeResult from(final Theme theme) {
        return new ThemeResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }
}
