package roomescape.controller.client.api.dto;

import roomescape.service.result.ThemeResult;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl
) {

    public static ThemeResponse from(ThemeResult themeResult) {
        return new ThemeResponse(themeResult.id(), themeResult.name(), themeResult.description(), themeResult.thumbnailImageUrl());
    }
}
