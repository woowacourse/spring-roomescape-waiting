package roomescape.controller.client.dto.response;

import roomescape.service.result.ThemeRegisterResult;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        Long price
) {

    public static ThemeResponse from(ThemeRegisterResult result) {
        return new ThemeResponse(result.id(), result.name(), result.description(), result.thumbnailImageUrl(),
                result.price());
    }
}
