package roomescape.theme.application.dto.response;

import roomescape.theme.domain.Theme;

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
