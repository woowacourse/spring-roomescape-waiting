package roomescape.controller.dto.response;

import roomescape.service.dto.result.ThemeResult;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {
    public static ThemeResponse from(final ThemeResult themeResult) {
        return new ThemeResponse(themeResult.id(), themeResult.name(), themeResult.description(), themeResult.thumbnail());
    }
}
