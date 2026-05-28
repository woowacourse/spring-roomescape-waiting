package roomescape.controller.client.api.dto;

import roomescape.service.result.ThemeRegisterResult;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl
) {

    public static ThemeResponse from(ThemeRegisterResult result) {
        return new ThemeResponse(result.id(), result.name(), result.description(), result.thumbnailImageUrl());
    }
}
