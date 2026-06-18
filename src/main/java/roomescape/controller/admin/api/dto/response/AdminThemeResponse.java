package roomescape.controller.admin.api.dto.response;

import roomescape.application.service.result.ThemeRegisterResult;

public record AdminThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        int price,
        boolean isActive
) {
    public AdminThemeResponse(long id, String name, String description, String thumbnailImageUrl, boolean isActive) {
        this(id, name, description, thumbnailImageUrl, 0, isActive);
    }

    public static AdminThemeResponse from(ThemeRegisterResult result) {
        return new AdminThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailImageUrl(),
                result.price(),
                true);
    }
}
