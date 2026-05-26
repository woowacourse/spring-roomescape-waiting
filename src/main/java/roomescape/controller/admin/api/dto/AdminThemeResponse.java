package roomescape.controller.admin.api.dto;

import roomescape.service.result.ThemeRegisterResult;
import roomescape.service.result.ThemeResult;

public record AdminThemeResponse(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        boolean isActive
) {
    public static AdminThemeResponse from(ThemeRegisterResult result) {
        return new AdminThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailImageUrl(),
                true);
    }

    public static AdminThemeResponse from(ThemeResult result) {
        return new AdminThemeResponse(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailImageUrl(),
                result.isActive());
    }
}
