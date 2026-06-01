package roomescape.theme.presentation.dto;

import roomescape.theme.application.dto.ThemeResult;

public record ThemeResponse(Long id, String name, String description, String thumbnailImgUrl) {

    public static ThemeResponse from(ThemeResult result) {
        return new ThemeResponse(result.id(), result.name(), result.description(), result.thumbnailImgUrl());
    }
}
