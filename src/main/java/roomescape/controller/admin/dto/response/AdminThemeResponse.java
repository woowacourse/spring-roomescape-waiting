package roomescape.controller.admin.dto.response;

import roomescape.service.result.ThemeRegisterResult;

public record AdminThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        Long price,
        boolean isActive
) {
    public static AdminThemeResponse from(ThemeRegisterResult result) {
        return new AdminThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailImageUrl(),
                result.price(),
                result.isActive());
    }
}
