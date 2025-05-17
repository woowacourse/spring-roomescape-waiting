package roomescape.theme.dto.response;

import roomescape.theme.entity.Theme;

public record ThemeReadResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeReadResponse from(Theme theme) {
        return new ThemeReadResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
