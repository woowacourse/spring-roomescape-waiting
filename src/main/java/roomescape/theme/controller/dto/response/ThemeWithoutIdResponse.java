package roomescape.theme.controller.dto.response;

import roomescape.theme.domain.Theme;

public record ThemeWithoutIdResponse(
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeWithoutIdResponse from(final Theme theme) {
        return new ThemeWithoutIdResponse(
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }
}
