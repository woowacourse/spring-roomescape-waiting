package roomescape.theme.dto.response;

import roomescape.theme.Theme;

public record ThemeSaveResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeSaveResponse from(Theme theme) {
        return new ThemeSaveResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }
}
