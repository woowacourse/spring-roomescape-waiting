package roomescape.controller.client.api.dto.response;

import roomescape.service.result.ThemeRegisterResult;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        int price
) {
    public ThemeResponse(long id, String name, String description, String thumbnailImageUrl) {
        this(id, name, description, thumbnailImageUrl, 0);
    }

    public static ThemeResponse from(ThemeRegisterResult result) {
        return new ThemeResponse(result.id(), result.name(), result.description(), result.thumbnailImageUrl(), result.price());
    }
}
