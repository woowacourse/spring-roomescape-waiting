package roomescape.controller.dto;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailUrl,
        Long price
) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.getPrice()
        );
    }
}
