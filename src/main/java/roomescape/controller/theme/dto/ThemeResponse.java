package roomescape.controller.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {

    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }
}
